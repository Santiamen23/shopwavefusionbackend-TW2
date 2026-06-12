package com.shopwavefusion.controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopwavefusion.exception.ProductException;
import com.shopwavefusion.modal.Product;
import com.shopwavefusion.request.CreateProductRequest;
import com.shopwavefusion.response.ApiResponse;
import com.shopwavefusion.service.ProductService;

@RestController
@RequestMapping("/admin/products")
public class AdminProductController {

	private ProductService productService;

	public AdminProductController(ProductService productService) {
		this.productService = productService;
	}

	@PostMapping("/")
	public ResponseEntity<Product> createProductHandler(@RequestBody CreateProductRequest req)
			throws ProductException, SQLException {
		Product createdProduct = productService.createProduct(req);
		return new ResponseEntity<Product>(createdProduct, HttpStatus.CREATED);
	}

	@DeleteMapping("/{productId}/delete")
	public ResponseEntity<ApiResponse> deleteProductHandler(@PathVariable Long productId) throws ProductException {
		String msg = productService.deleteProduct(productId);
		ApiResponse res = new ApiResponse(msg, true);
		return new ResponseEntity<ApiResponse>(res, HttpStatus.OK);
	}

	@PutMapping("/{productId}/update")
	public ResponseEntity<Product> updateProductHandler(@RequestBody Product req, @PathVariable Long productId)
			throws ProductException {
		Product updatedProduct = productService.updateProduct(productId, req);
		return new ResponseEntity<Product>(updatedProduct, HttpStatus.OK);
	}

	@PostMapping("/creates")
	public ResponseEntity<ApiResponse> createMultipleProduct(@RequestBody List<CreateProductRequest> reqs)
			throws ProductException, SQLException {
		for (CreateProductRequest product : reqs) {
			productService.createProduct(product);
		}
		ApiResponse res = new ApiResponse("products created successfully", true);
		return new ResponseEntity<ApiResponse>(res, HttpStatus.CREATED);
	}
}

