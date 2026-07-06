package com.shopsphere.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopsphere.backend.entity.CartItem;
import com.shopsphere.backend.entity.Order;
import com.shopsphere.backend.entity.OrderItem;
import com.shopsphere.backend.entity.Product;
import com.shopsphere.backend.entity.User;
import com.shopsphere.backend.repository.CartRepository;
import com.shopsphere.backend.repository.OrderItemRepository;
import com.shopsphere.backend.repository.OrderRepository;
import com.shopsphere.backend.repository.ProductRepository;
import com.shopsphere.backend.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private OrderItemRepository orderItemRepository;
	
	@PostMapping
	@Transactional
	public Order placeOrder(HttpServletRequest request) {

	    String email = (String) request.getAttribute("email");

	    User user = userRepository.findByEmail(email);

	    if (user == null) {
	        throw new RuntimeException("User not found");
	    }

	    Integer userId = user.getId();

	    List<CartItem> cartItems =
	            cartRepository.findByUserId(userId);

	    if (cartItems.isEmpty()) {
	        throw new RuntimeException("Cart is empty");
	    }

	    double total = 0;

	    // CHECK STOCK AND CALCULATE TOTAL
	    for (CartItem item : cartItems) {

	        Product product = productRepository
	                .findById(item.getProduct().getId())
	                .orElseThrow(
	                        () -> new RuntimeException(
	                                "Product not found"
	                        )
	                );

	        if (product.getStock() < item.getQuantity()) {
	            throw new RuntimeException(
	                    "Insufficient stock for "
	                    + product.getName()
	            );
	        }

	        total +=
	                item.getQuantity()
	                * product.getPrice();
	    }

	    // CREATE ORDER
	    Order order = new Order(
	            userId,
	            total,
	            "PLACED"
	    );

	    order.setCreatedAt(
	            LocalDateTime.now()
	    );

	    Order savedOrder =
	            orderRepository.save(order);

	    // SAVE ORDER ITEMS AND REDUCE STOCK
	    for (CartItem item : cartItems) {

	        Product product = productRepository
	                .findById(item.getProduct().getId())
	                .orElseThrow(
	                        () -> new RuntimeException(
	                                "Product not found"
	                        )
	                );

	        OrderItem orderItem =
	                new OrderItem(
	                        savedOrder.getId(),
	                        product.getId(),
	                        item.getQuantity(),
	                        product.getPrice(),
	                        product.getName(),
	                        product.getImageUrl()
	                );

	        orderItemRepository.save(orderItem);

	        // REDUCE PRODUCT STOCK
	        product.setStock(
	                product.getStock()
	                - item.getQuantity()
	        );

	        productRepository.save(product);
	    }

	    // CLEAR CART
	    cartRepository.deleteAll(cartItems);

	    return savedOrder;
	}
	
	@GetMapping
	public List<Order> getMyOrders(HttpServletRequest request) {

	    // ✅ get email from JWT
	    String email = (String) request.getAttribute("email");

		// ✅ get user from DB
	    User user = userRepository.findByEmail(email);
	    
	    // ✅ fetch orders
	    return orderRepository.findByUserId(user.getId());
	}
	
	@GetMapping("/{orderId}/items")
	public List<OrderItem> getOrderItems(@PathVariable Integer orderId) {
		List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
		for (OrderItem item : items) {
			Product product = productRepository.findById(item.getProductId()).orElse(null);
			
			if (product != null) {
				item.setProductName(product.getName());
				item.setImageUrl(product.getImageUrl());
			}
		}
		return items;
	}
	
	@GetMapping("/admin") 
	public List<Order> getAllOrders(HttpServletRequest request) {
		String role = (String) request.getAttribute("role");
		
		if (!"ADMIN".equals(role)) {
			throw new RuntimeException("Only ADMIN can view all orders");
		}
		
		List<Order> orders = orderRepository.findAll();
		for (Order order: orders) {
			User user = userRepository.findById(order.getUserId()).orElse(null);
			order.setUser(user);
			List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
			for (OrderItem item : items) {
				Product product = productRepository.findById(item.getProductId()).orElse(null);
				if (product != null) {
					item.setProductName(product.getName());
					item.setImageUrl(product.getImageUrl());
				}
			}
			order.setItems(items);
		}
		return orders;
	}
	
	@PostMapping("/admin/{orderId}/status")
	public Order updateStatus(
			@PathVariable Integer orderId,
			@RequestParam String status,
			HttpServletRequest request) {
		
		String role = (String) request.getAttribute("role");
		
		if (!"ADMIN".equals(role)) {
			throw new RuntimeException("Only ADMIN can update status");
		}
		
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new RuntimeException("Order not found"));
		
		order.setStatus(status);
		
		return orderRepository.save(order);
	}
}
