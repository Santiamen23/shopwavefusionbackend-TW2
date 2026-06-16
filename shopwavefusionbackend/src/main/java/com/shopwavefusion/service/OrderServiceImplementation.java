package com.shopwavefusion.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopwavefusion.exception.OrderException;
import com.shopwavefusion.modal.Address;
import com.shopwavefusion.modal.Cart;
import com.shopwavefusion.modal.CartItem;
import com.shopwavefusion.modal.Order;
import com.shopwavefusion.modal.OrderItem;
import com.shopwavefusion.modal.Product;
import com.shopwavefusion.modal.Size;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.repository.AddressRepository;
import com.shopwavefusion.repository.CartItemRepository;
import com.shopwavefusion.repository.CartRepository;
import com.shopwavefusion.repository.OrderItemRepository;
import com.shopwavefusion.repository.OrderRepository;
import com.shopwavefusion.repository.ProductRepository;
import com.shopwavefusion.repository.UserRepository;
import com.shopwavefusion.request.CreateOrderRequest;
import com.shopwavefusion.user.domain.OrderStatus;
import com.shopwavefusion.user.domain.PaymentStatus;

@Service
public class OrderServiceImplementation implements OrderService {

	private OrderRepository orderRepository;
	private CartService cartService;
	private AddressRepository addressRepository;
	private UserRepository userRepository;
	private OrderItemService orderItemService;
	private OrderItemRepository orderItemRepository;
	private CartRepository cartRepository;
	private CartItemRepository cartItemRepository;
	private ProductRepository productRepository;

	public OrderServiceImplementation(OrderRepository orderRepository, CartService cartService,
			AddressRepository addressRepository, UserRepository userRepository,
			OrderItemService orderItemService, OrderItemRepository orderItemRepository,
			CartRepository cartRepository, CartItemRepository cartItemRepository,
			ProductRepository productRepository) {
		this.orderRepository = orderRepository;
		this.cartService = cartService;
		this.addressRepository = addressRepository;
		this.userRepository = userRepository;
		this.orderItemService = orderItemService;
		this.orderItemRepository = orderItemRepository;
		this.cartRepository = cartRepository;
		this.cartItemRepository = cartItemRepository;
		this.productRepository = productRepository;
	}

	@Override
	@Transactional
	public Order createOrder(User user, CreateOrderRequest orderRequest) throws OrderException {
		Cart cart = cartService.findUserCart(user.getId());

		if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
			throw new OrderException("No se puede crear una orden con un carrito vacio.");
		}

		Address shippAddress = new Address();
		shippAddress.setCity(orderRequest.getCity());
		shippAddress.setFirstName(orderRequest.getFirstName());
		shippAddress.setLastName(orderRequest.getLastName());
		shippAddress.setMobile(orderRequest.getMobile());
		shippAddress.setState(orderRequest.getState());
		shippAddress.setStreetAddress(orderRequest.getStreetAddress());
		shippAddress.setZipCode(orderRequest.getZipCode());
		shippAddress.setUser(user);
		Address address = addressRepository.save(shippAddress);

		List<OrderItem> orderItems = new ArrayList<>();
		List<Long> cartItemIds = new ArrayList<>();

		for (CartItem item : cart.getCartItems()) {
			OrderItem orderItem = new OrderItem();

			orderItem.setPrice(item.getPrice());
			orderItem.setProduct(item.getProduct());
			orderItem.setQuantity(item.getQuantity());
			orderItem.setSize(item.getSize());
			orderItem.setUserId(item.getUserId());
			orderItem.setDiscountedPrice(item.getDiscountedPrice());

			orderItems.add(orderItem);
			cartItemIds.add(item.getId());
		}

		Order createdOrder = new Order();
		createdOrder.setUser(user);
		createdOrder.setOrderId(generateReadableOrderId());
		createdOrder.setOrderItems(orderItems);
		createdOrder.setTotalPrice(cart.getTotalPrice());
		createdOrder.setTotalDiscountedPrice(cart.getTotalDiscountedPrice());
		createdOrder.setDiscounte(cart.getDiscounte());
		createdOrder.setTotalItem(cart.getTotalItem());

		createdOrder.setShippingAddress(address);
		createdOrder.setOrderDate(LocalDateTime.now());
		createdOrder.setOrderStatus(OrderStatus.PLACED);
		createdOrder.getPaymentDetails().setStatus(PaymentStatus.COMPLETED);
		createdOrder.getPaymentDetails().setCardholderName(orderRequest.getCardholderName());
		createdOrder.getPaymentDetails().setCardNumber(orderRequest.getCardNumber());
		createdOrder.getPaymentDetails().setPaymentMethod(orderRequest.getPaymentMethod());
		createdOrder.getPaymentDetails()
				.setPaymentId(orderRequest.getPaymentId() != null && !orderRequest.getPaymentId().isBlank()
						? orderRequest.getPaymentId()
						: generatePaymentId());
		createdOrder.setCreatedAt(LocalDateTime.now());

		Order savedOrder = orderRepository.save(createdOrder);

		for (OrderItem item : orderItems) {
			item.setOrder(savedOrder);
			orderItemRepository.save(item);
		}

		for (OrderItem item : orderItems) {
			Product product = productRepository.findById(item.getProduct().getId())
					.orElseThrow(() -> new OrderException("Product not found with id " + item.getProduct().getId()));

			int qty = item.getQuantity();
			int stockBefore = product.getQuantity();

			if (product.getQuantity() < qty) {
				throw new OrderException("Stock insuficiente para el producto '" + product.getTitle()
						+ "'. Disponible: " + product.getQuantity() + ", solicitado: " + qty);
			}
			product.setQuantity(product.getQuantity() - qty);

			if (item.getSize() != null && !item.getSize().isBlank() && product.getSizes() != null) {
				Size sizeMatch = product.getSizes().stream()
						.filter(s -> s.getName().equalsIgnoreCase(item.getSize()))
						.findFirst()
						.orElseThrow(() -> new OrderException("Talla '" + item.getSize()
								+ "' no existe para el producto '" + product.getTitle() + "'"));
				if (sizeMatch.getQuantity() < qty) {
					throw new OrderException("Stock insuficiente para la talla '" + sizeMatch.getName()
							+ "' del producto '" + product.getTitle()
							+ "'. Disponible: " + sizeMatch.getQuantity() + ", solicitado: " + qty);
				}
				sizeMatch.setQuantity(sizeMatch.getQuantity() - qty);
			}

			Product saved = productRepository.save(product);
			System.out.println("[STOCK] product=" + saved.getId() + " title='" + saved.getTitle()
					+ "' size='" + item.getSize() + "' before=" + stockBefore
					+ " after=" + saved.getQuantity() + " delta=-" + qty);
		}

		for (Long id : cartItemIds) {
			cartItemRepository.deleteCartItemById(id);
		}
		cart.setTotalPrice(0);
		cart.setTotalDiscountedPrice(0);
		cart.setDiscounte(0);
		cart.setTotalItem(0);
		cartRepository.save(cart);

		return savedOrder;
	}

	@Override
	@Transactional
	public Order placedOrder(Long orderId) throws OrderException {
		Order order = findOrderById(orderId);
		order.setOrderStatus(OrderStatus.PLACED);
		order.getPaymentDetails().setStatus(PaymentStatus.COMPLETED);
		return orderRepository.save(order);
	}

	@Override
	@Transactional
	public Order confirmedOrder(Long orderId) throws OrderException {
		Order order = findOrderById(orderId);
		order.setOrderStatus(OrderStatus.CONFIRMED);
		return orderRepository.save(order);
	}

	@Override
	@Transactional
	public Order shippedOrder(Long orderId) throws OrderException {
		Order order = findOrderById(orderId);
		order.setOrderStatus(OrderStatus.SHIPPED);
		return orderRepository.save(order);
	}

	@Override
	@Transactional
	public Order deliveredOrder(Long orderId) throws OrderException {
		Order order = findOrderById(orderId);
		order.setOrderStatus(OrderStatus.DELIVERED);
		return orderRepository.save(order);
	}

	@Override
	@Transactional
	public Order cancledOrder(Long orderId) throws OrderException {
		Order order = findOrderById(orderId);
		order.setOrderStatus(OrderStatus.CANCELLED);
		return orderRepository.save(order);
	}

	@Override
	@Transactional(readOnly = true)
	public Order findOrderById(Long orderId) throws OrderException {
		Optional<Order> opt = orderRepository.findById(orderId);

		if (opt.isPresent()) {
			return opt.get();
		}
		throw new OrderException("order not exist with id " + orderId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Order> usersOrderHistory(Long userId) {
		return orderRepository.getUsersOrders(userId);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

	@Override
	@Transactional
	public void deleteOrder(Long orderId) throws OrderException {
		orderRepository.deleteById(orderId);
	}

	public static String generatePaymentId() {
		String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		int LENGTH = 30;

		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder(LENGTH);
		for (int i = 0; i < LENGTH; i++) {
			int randomIndex = random.nextInt(CHARACTERS.length());
			char randomChar = CHARACTERS.charAt(randomIndex);
			sb.append(randomChar);
		}
		return sb.toString();
	}

	public static String generateReadableOrderId() {
		SecureRandom random = new SecureRandom();
		long number = (long) (random.nextDouble() * 9_000_000_000L) + 1_000_000_000L;
		return "SW-" + number;
	}
}

