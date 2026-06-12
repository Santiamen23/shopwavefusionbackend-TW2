package com.shopwavefusion.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopwavefusion.exception.CartItemException;
import com.shopwavefusion.exception.UserException;
import com.shopwavefusion.modal.Cart;
import com.shopwavefusion.modal.CartItem;
import com.shopwavefusion.modal.Product;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.repository.CartItemRepository;
import com.shopwavefusion.repository.CartRepository;

@Service
public class CartItemServiceImplementation implements CartItemService {

	private CartItemRepository cartItemRepository;
	private UserService userService;
	private CartRepository cartRepository;

	public CartItemServiceImplementation(CartItemRepository cartItemRepository, UserService userService) {
		this.cartItemRepository = cartItemRepository;
		this.userService = userService;
	}

	@Override
	@Transactional
	public CartItem createCartItem(CartItem cartItem) {

		if (cartItem.getQuantity() <= 0) {
			cartItem.setQuantity(1);
		}
		if (cartItem.getPrice() == null || cartItem.getPrice() <= 0) {
			cartItem.setPrice(cartItem.getProduct().getPrice() * cartItem.getQuantity());
		}
		if (cartItem.getDiscountedPrice() == null || cartItem.getDiscountedPrice() <= 0) {
			cartItem.setDiscountedPrice(cartItem.getProduct().getDiscountedPrice() * cartItem.getQuantity());
		}

		return cartItemRepository.save(cartItem);
	}

	@Override
	@Transactional
	public CartItem updateCartItem(Long userId, Long id, CartItem cartItem) throws CartItemException, UserException {

		CartItem item = findCartItemById(id);
		User user = userService.findUserById(item.getUserId());

		if (user.getId().equals(userId)) {

			int newQuantity = cartItem.getQuantity() > 0 ? cartItem.getQuantity() : 1;
			item.setQuantity(newQuantity);
			item.setPrice(item.getQuantity() * item.getProduct().getPrice());
			item.setDiscountedPrice(item.getQuantity() * item.getProduct().getDiscountedPrice());

			return cartItemRepository.save(item);

		} else {
			throw new CartItemException("You can't update  another users cart_item");
		}

	}

	@Override
	public CartItem isCartItemExist(Cart cart, Product product, String size, Long userId) {

		return cartItemRepository.isCartItemExist(cart, product, size, userId);
	}

	@Override
	@Transactional
	public void removeCartItem(Long userId, Long cartItemId) throws CartItemException, UserException {

		CartItem cartItem = findCartItemById(cartItemId);

		User user = userService.findUserById(cartItem.getUserId());
		User reqUser = userService.findUserById(userId);

		if (user.getId().equals(reqUser.getId())) {
			cartItemRepository.deleteById(cartItem.getId());
		} else {
			throw new UserException("you can't remove anothor users item");
		}

	}

	@Override
	public CartItem findCartItemById(Long cartItemId) throws CartItemException {
		Optional<CartItem> opt = cartItemRepository.findById(cartItemId);

		if (opt.isPresent()) {
			return opt.get();
		}
		throw new CartItemException("cartItem not found with id : " + cartItemId);
	}

}

