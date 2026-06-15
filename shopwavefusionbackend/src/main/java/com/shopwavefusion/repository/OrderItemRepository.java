package com.shopwavefusion.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopwavefusion.modal.OrderItem;
import com.shopwavefusion.modal.Product;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

	@org.springframework.transaction.annotation.Transactional
	void deleteByProduct(Product product);

}
