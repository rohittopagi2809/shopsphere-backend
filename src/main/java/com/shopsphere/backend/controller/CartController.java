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
	
	// Add to cart
	@PostMapping
	public CartItem addToCart(
	        @RequestBody CartItem item,
	        HttpServletRequest request) {

	    String email =
	            (String) request.getAttribute("email");

	    User user =
	            userRepository.findByEmail(email);

	    if (user == null) {
	        throw new RuntimeException("User not found");
	    }

	    Product product =
	            productRepository
	                    .findById(item.getProduct().getId())
	                    .orElseThrow(
	                            () -> new RuntimeException(
	                                    "Product not found"
	                            )
	                    );

	    if (product.getStock() <= 0) {
	        throw new RuntimeException(
	                "Product is out of stock"
	        );
	    }

	    CartItem existingItem =
	            cartRepository
	                    .findByUserIdAndProduct_Id(
	                            user.getId(),
	                            product.getId()
	                    );

	    // Product already in cart
	    if (existingItem != null) {

	        int newQuantity =
	                existingItem.getQuantity() + 1;

	        if (newQuantity > product.getStock()) {
	            throw new RuntimeException(
	                    "Only "
	                    + product.getStock()
	                    + " items available"
	            );
	        }

	        existingItem.setQuantity(newQuantity);

	        return cartRepository.save(existingItem);
	    }

	    // New product in cart
	    item.setProduct(product);
	    item.setUserId(user.getId());
	    item.setQuantity(1);

	    return cartRepository.save(item);
	}
	
	// Remove item
	@DeleteMapping("/{id}")
	public String removeItem(
	        @PathVariable Integer id,
	        HttpServletRequest request) {

	    String email =
	            (String) request.getAttribute("email");

	    User user =
	            userRepository.findByEmail(email);

	    if (user == null) {
	        throw new RuntimeException("User not found");
	    }

	    CartItem item =
	            cartRepository.findById(id)
	                    .orElseThrow(
	                            () -> new RuntimeException(
	                                    "Cart item not found"
	                            )
	                    );

	    if (!item.getUserId().equals(user.getId())) {
	        throw new RuntimeException(
	                "You cannot remove this cart item"
	        );
	    }

	    cartRepository.delete(item);

	    return "Item removed";
	}


	// Increase quantity
	@PutMapping("/increase/{id}")
	public CartItem increaseQty(
	        @PathVariable Integer id,
	        HttpServletRequest request) {

	    String email =
	            (String) request.getAttribute("email");

	    User user =
	            userRepository.findByEmail(email);

	    if (user == null) {
	        throw new RuntimeException("User not found");
	    }

	    CartItem item =
	            cartRepository.findById(id)
	                    .orElseThrow(
	                            () -> new RuntimeException(
	                                    "Cart item not found"
	                            )
	                    );

	    if (!item.getUserId().equals(user.getId())) {
	        throw new RuntimeException(
	                "You cannot update this cart item"
	        );
	    }

	    Product product = item.getProduct();

	    if (item.getQuantity() >= product.getStock()) {
	        throw new RuntimeException(
	                "Only "
	                + product.getStock()
	                + " items available"
	        );
	    }

	    item.setQuantity(
	            item.getQuantity() + 1
	    );

	    return cartRepository.save(item);
	}


	// Decrease quantity
	@PutMapping("/decrease/{id}")
	public CartItem decreaseQty(
	        @PathVariable Integer id,
	        HttpServletRequest request) {

	    String email =
	            (String) request.getAttribute("email");

	    User user =
	            userRepository.findByEmail(email);

	    if (user == null) {
	        throw new RuntimeException("User not found");
	    }

	    CartItem item =
	            cartRepository.findById(id)
	                    .orElseThrow(
	                            () -> new RuntimeException(
	                                    "Cart item not found"
	                            )
	                    );

	    if (!item.getUserId().equals(user.getId())) {
	        throw new RuntimeException(
	                "You cannot update this cart item"
	        );
	    }

	    if (item.getQuantity() > 1) {

	        item.setQuantity(
	                item.getQuantity() - 1
	        );

	        return cartRepository.save(item);
	    }

	    cartRepository.delete(item);

	    return null;
	}
}
