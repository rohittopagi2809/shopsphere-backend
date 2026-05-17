package com.shopsphere.backend.controller;

import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import org.springframework.web.multipart.MultipartFile;

import com.shopsphere.backend.entity.Product;
import com.shopsphere.backend.repository.ProductRepository;


import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/products")
public class ProductController {
	
	@Autowired
	private ProductRepository productRepository;
	
	// Get All Products
	@GetMapping
	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}
	
	@GetMapping("/search")
	public List<Product> searchPRoducts(@RequestParam String keyword) {
		return productRepository.findByNameContainingIgnoreCase(keyword);
	}
	
	// Get product by ID
	@GetMapping("/{id}")
	public Product getProductById(@PathVariable Integer id) {
		return productRepository.findById(id).orElse(null);
	}
	
	// Add Product
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Product addProduct(@RequestParam String name, 
			@RequestParam double price, 
			@RequestParam int stock, 
			@RequestParam String category,
			@RequestParam(required = false) MultipartFile image, HttpServletRequest request) throws Exception {
			
		String role = (String) request.getAttribute("role");
			
		if (!"ADMIN".equals(role)) {
			throw new RuntimeException("only ADMIN can add products");
		}
		
		String imageName = null;
		
		//Image Upload
		if (image != null && !image.isEmpty()) {
			imageName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
			
			Path uploadPath = Paths.get("uploads");
			
			//create uploads folder
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			
			Path filePath = uploadPath.resolve(imageName);
			Files.write(filePath, image.getBytes());
		}
		
		//Create Product
		Product product = new Product();
		product.setName(name);
		product.setPrice(price);
		product.setStock(stock);
		product.setCategory(category);
		
		product.setImageUrl(imageName);
		return productRepository.save(product);
			
	}
	
	// Delete Product	
	@DeleteMapping("/{id}")
	public String deleteProduct(@PathVariable Integer id, HttpServletRequest request) {

	    String role = (String) request.getAttribute("role");

	    if (!"ADMIN".equals(role)) {
	        throw new RuntimeException("Only ADMIN can delete products");
	    }

	    productRepository.deleteById(id);

	    return "Deleted";
	}
	
	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Product updateProduct(@PathVariable Integer id, 
			@RequestParam String name, 
			@RequestParam Double price,
			@RequestParam int stock,
			@RequestParam String category,
			@RequestParam(required = false) MultipartFile image) throws IOException {
		Product product = productRepository.findById(id).orElseThrow();
		product.setName(name);
		product.setPrice(price);
		product.setStock(stock);
		product.setCategory(category);
		
		if (image != null && !image.isEmpty()) {
			String imageName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
			Path uploadPath = Paths.get("uploads");
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			Files.copy(image.getInputStream(), uploadPath.resolve(imageName), StandardCopyOption.REPLACE_EXISTING);
			product.setImageUrl(imageName);
		}
		
		return productRepository.save(product);		
	}
	
}
