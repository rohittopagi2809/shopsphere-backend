package com.shopsphere.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopsphere.backend.entity.CartItem;
import com.shopsphere.backend.entity.Product;
import com.shopsphere.backend.entity.User;
import com.shopsphere.backend.repository.CartRepository;
import com.shopsphere.backend.repository.ProductRepository;
import com.shopsphere.backend.repository.UserRepository;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/cart")
public class CartController {
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	// View cart
	@GetMapping
	public List<CartItem> getCart(HttpServletRequest request) {
		String email = (String) request.getAttribute("email");
		User user = userRepository.findByEmail(email);
		return cartRepository.findByUserId(user.getId());
	}
	
	// Remove item
	@DeleteMapping("/{id}")
	public String removeItem(@PathVariable Integer id) {
		cartRepository.deleteById(id);
		return "Item removed";
	}
	
	// Add to cart
	@PostMapping
	public CartItem addToCart(@RequestBody CartItem item, HttpServletRequest request) {
		
		String email = (String) request.getAttribute("email");
		User user = userRepository.findByEmail(email);
		Product product = productRepository.findById(item.getProduct().getId()).orElseThrow();
		item.setProduct(product);
		item.setUserId(user.getId());
		return cartRepository.save(item);
	}
	
	@PutMapping("/increase/{id}")
	public CartItem increaseQty(@PathVariable Integer id) {
		CartItem item = cartRepository.findById(id).orElseThrow();
		item.setQuantity(item.getQuantity() + 1);
		return cartRepository.save(item);
	}
	
	@PutMapping("/decrease/{id}")
	public CartItem decreaseQty(@PathVariable Integer id) {
		CartItem item = cartRepository.findById(id).orElseThrow();
		if (item.getQuantity() > 1) {
			item.setQuantity(item.getQuantity() - 1);
			return cartRepository.save(item);
		}
		cartRepository.delete(item);
		return null;
	}
}
