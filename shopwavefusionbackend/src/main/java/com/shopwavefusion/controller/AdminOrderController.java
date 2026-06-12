package com.shopwavefusion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopwavefusion.exception.OrderException;
import com.shopwavefusion.modal.Order;
import com.shopwavefusion.response.ApiResponse;
import com.shopwavefusion.service.OrderService;

@RestController
@RequestMapping("/admin/orders")
public class AdminOrderController {

	private OrderService orderService;

	public AdminOrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@GetMapping("/")
	public ResponseEntity<List<Order>> getAllOrdersHandler() {
		List<Order> orders = orderService.getAllOrders();
		return new ResponseEntity<>(orders, HttpStatus.OK);
	}

	@PutMapping("/{orderId}/confirmed")
	public ResponseEntity<Order> ConfirmedOrderHandler(@PathVariable Long orderId) throws OrderException {
		Order order = orderService.confirmedOrder(orderId);
		return new ResponseEntity<Order>(order, HttpStatus.OK);
	}

	@PutMapping("/{orderId}/ship")
	public ResponseEntity<Order> shippedOrderHandler(@PathVariable Long orderId) throws OrderException {
		Order order = orderService.shippedOrder(orderId);
		return new ResponseEntity<Order>(order, HttpStatus.OK);
	}

	@PutMapping("/{orderId}/deliver")
	public ResponseEntity<Order> deliveredOrderHandler(@PathVariable Long orderId) throws OrderException {
		Order order = orderService.deliveredOrder(orderId);
		return new ResponseEntity<Order>(order, HttpStatus.OK);
	}

	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<Order> canceledOrderHandler(@PathVariable Long orderId) throws OrderException {
		Order order = orderService.cancledOrder(orderId);
		return new ResponseEntity<Order>(order, HttpStatus.OK);
	}

	@DeleteMapping("/{orderId}/delete")
	public ResponseEntity<ApiResponse> deleteOrderHandler(@PathVariable Long orderId) throws OrderException {
		orderService.deleteOrder(orderId);
		ApiResponse res = new ApiResponse("Order Deleted Successfully", true);
		return new ResponseEntity<>(res, HttpStatus.OK);
	}
}

