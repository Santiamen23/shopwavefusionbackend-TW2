package com.shopwavefusion.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopwavefusion.modal.CartItem;
import com.shopwavefusion.repository.CartItemRepository;
import com.shopwavefusion.response.ApiResponse;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/admin/cart-items")
public class AdminCartItemController {

	private CartItemRepository cartItemRepository;

	public AdminCartItemController(CartItemRepository cartItemRepository) {
		this.cartItemRepository = cartItemRepository;
	}

	@GetMapping("/orphaned")
	public ResponseEntity<List<CartItem>> getOrphanedCartItems() {
		List<CartItem> all = cartItemRepository.findAll();
		List<CartItem> orphaned = all.stream()
				.filter(ci -> ci.getProduct() == null)
				.collect(Collectors.toList());
		return new ResponseEntity<>(orphaned, HttpStatus.OK);
	}

	@DeleteMapping("/{cartItemId}")
	@Transactional
	public ResponseEntity<ApiResponse> deleteCartItem(@PathVariable Long cartItemId) {
		int rows = cartItemRepository.deleteCartItemById(cartItemId);
		if (rows == 0) {
			return new ResponseEntity<>(new ApiResponse("CartItem not found", false), HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(new ApiResponse("CartItem deleted", true), HttpStatus.OK);
	}
}
