package com.shopwavefusion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopwavefusion.exception.ProductException;
import com.shopwavefusion.exception.UserException;
import com.shopwavefusion.modal.Review;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.request.ReviewRequest;
import com.shopwavefusion.service.ReviewService;
import com.shopwavefusion.service.UserService;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

	private ReviewService reviewService;
	private UserService userService;

	public ReviewController(ReviewService reviewService, UserService userService) {
		this.reviewService = reviewService;
		this.userService = userService;
	}

	@PostMapping("/create")
	public ResponseEntity<Review> createReviewHandler(@RequestBody ReviewRequest req,
			@RequestHeader("Authorization") String jwt) throws UserException, ProductException {
		User user = userService.findUserProfileByJwt(jwt);
		Review review = reviewService.createReview(req, user);
		return new ResponseEntity<Review>(review, HttpStatus.CREATED);
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<List<Review>> getProductsReviewHandler(@PathVariable Long productId) {
		List<Review> reviews = reviewService.getAllReview(productId);
		return new ResponseEntity<List<Review>>(reviews, HttpStatus.OK);
	}

}

