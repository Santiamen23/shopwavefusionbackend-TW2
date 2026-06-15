package com.shopwavefusion.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopwavefusion.exception.CartItemException;
import com.shopwavefusion.exception.ProductException;
import com.shopwavefusion.exception.UserException;
import com.shopwavefusion.modal.Cart;
import com.shopwavefusion.modal.CartItem;
import com.shopwavefusion.modal.Product;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.repository.CartItemRepository;
import com.shopwavefusion.repository.CartRepository;
import com.shopwavefusion.repository.UserRepository;
import com.shopwavefusion.request.AddItemRequest;

@Service
public class CartServiceImplementation implements CartService {

	private CartRepository cartRepository;
	private CartItemService cartItemService;
	private ProductService productService;
	private UserRepository userRepository;
	private CartItemRepository cartItemRepository;

	public CartServiceImplementation(CartRepository cartRepository, CartItemService cartItemService,
			ProductService productService, UserRepository userRepository, CartItemRepository cartItemRepository) {
		this.cartRepository = cartRepository;
		this.productService = productService;
		this.cartItemService = cartItemService;
		this.userRepository = userRepository;
		this.cartItemRepository = cartItemRepository;
	}

	@Override
	public Cart createCart(User user) {
		Cart existing = cartRepository.findByUserId(user.getId());
		if (existing != null) {
			return existing;
		}
		Cart cart = new Cart();
		cart.setUser(user);
		return cartRepository.save(cart);
	}

	@Override
	@Transactional
	public Cart findUserCart(Long userId) {
		Cart cart = cartRepository.findByUserId(userId);
		if (cart == null) {
			User user = userRepository.findById(userId).orElse(null);
			if (user == null) {
				return null;
			}
			cart = createCart(user);
		}

		int totalPrice = 0;
		int totalDiscountedPrice = 0;
		int totalItem = 0;
		int totalQuantity = 0;

		java.util.Iterator<CartItem> it = cart.getCartItems().iterator();
		while (it.hasNext()) {
			CartItem item = it.next();
			if (item.getProduct() == null) {
				cartItemRepository.deleteById(item.getId());
				it.remove();
				continue;
			}
			totalPrice += item.getPrice() != null ? item.getPrice() : 0;
			totalDiscountedPrice += item.getDiscountedPrice() != null ? item.getDiscountedPrice() : 0;
			totalItem += 1;
			totalQuantity += item.getQuantity();
		}

		cart.setTotalPrice(totalPrice);
		cart.setTotalItem(totalQuantity);
		cart.setTotalDiscountedPrice(totalDiscountedPrice);
		cart.setDiscounte(totalPrice - totalDiscountedPrice);

		return cartRepository.save(cart);
	}

	@Override
	@Transactional
	public CartItem addCartItem(Long userId, AddItemRequest req) throws ProductException, CartItemException, UserException {
		if (req.getSize() == null || req.getSize().isBlank()) {
			throw new CartItemException("size is required");
		}
		if (req.getQuantity() <= 0) {
			throw new CartItemException("quantity must be greater than 0");
		}

		Cart cart = cartRepository.findByUserId(userId);
		if (cart == null) {
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new ProductException("User not found with id " + userId));
			cart = createCart(user);
		}
		Product product = productService.findProductById(req.getProductId());

		CartItem isPresent = cartItemService.isCartItemExist(cart, product, req.getSize(), userId);
		CartItem createdCartItem = null;
		if (isPresent == null) {
			CartItem cartItem = new CartItem();
			cartItem.setProduct(product);
			cartItem.setCart(cart);
			cartItem.setQuantity(req.getQuantity());
			cartItem.setUserId(userId);
			cartItem.setSize(req.getSize());

			int unitPrice = product.getDiscountedPrice();
			if (req.getPrice() != null && req.getPrice() > 0) {
				unitPrice = req.getPrice() / req.getQuantity();
			}
			cartItem.setPrice(req.getQuantity() * product.getPrice());
			cartItem.setDiscountedPrice(req.getQuantity() * unitPrice);

			createdCartItem = cartItemService.createCartItem(cartItem);
			cart.getCartItems().add(createdCartItem);
		} else {
			isPresent.setQuantity(isPresent.getQuantity() + req.getQuantity());
			isPresent.setPrice(isPresent.getQuantity() * isPresent.getProduct().getPrice());
			isPresent.setDiscountedPrice(isPresent.getQuantity() * isPresent.getProduct().getDiscountedPrice());
			createdCartItem = cartItemService.updateCartItem(userId, isPresent.getId(), isPresent);
			cart.getCartItems().remove(isPresent);
			cart.getCartItems().add(createdCartItem);
		}

		return createdCartItem;
	}

}

