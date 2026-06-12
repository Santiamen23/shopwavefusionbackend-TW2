package com.shopwavefusion.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopwavefusion.exception.ProductException;
import com.shopwavefusion.modal.Product;
import com.shopwavefusion.modal.Rating;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.repository.ProductRepository;
import com.shopwavefusion.repository.RatingRepository;
import com.shopwavefusion.request.RatingRequest;

@Service
public class RatingServiceImplementation implements RatingServices {

	private RatingRepository ratingRepository;
	private ProductService productService;
	private ProductRepository productRepository;

	public RatingServiceImplementation(RatingRepository ratingRepository, ProductService productService,
			ProductRepository productRepository) {
		this.ratingRepository = ratingRepository;
		this.productService = productService;
		this.productRepository = productRepository;
	}

	@Override
	@Transactional
	public Rating createRating(RatingRequest req, User user) throws ProductException {

		Product product = productService.findProductById(req.getProductId());

		Rating rating = new Rating();
		rating.setProduct(product);
		rating.setUser(user);
		rating.setRating(req.getRating());
		rating.setCreatedAt(LocalDateTime.now());

		Rating saved = ratingRepository.save(rating);

		product.setNumRatings(product.getNumRatings() + 1);
		productRepository.save(product);

		return saved;
	}

	@Override
	@Transactional(readOnly = true)
	public List<Rating> getProductsRating(Long productId) {
		return ratingRepository.getAllProductsRating(productId);
	}

}

