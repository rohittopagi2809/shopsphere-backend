package com.shopsphere.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopsphere.backend.entity.Order;
import com.shopsphere.backend.entity.Product;
import com.shopsphere.backend.entity.User;
import com.shopsphere.backend.repository.OrderRepository;
import com.shopsphere.backend.repository.ProductRepository;
import com.shopsphere.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/stats")
	public Map<String, Object> getStats() {
		List<Product> products = productRepository.findAll();
		List<Order> orders = orderRepository.findAll();
		List<User> users = userRepository.findAll();
		
		double revenue = orders.stream().mapToDouble(Order::getTotalAmount).sum();
		long pendingOrders = orders.stream().filter(o -> o.getStatus() != null && 
				o.getStatus().equalsIgnoreCase("Placed")).count();
		
		Map<String, Object> data = new HashMap<>();
		data.put("totalProducts", products.size());
		data.put("totalOrders", orders.size());
		data.put("totalUsers", users.size());
		data.put("totalRevenue", revenue);
		data.put("pendingOrders", pendingOrders);
		
		return data;
	}
}
