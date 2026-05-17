package com.shopsphere.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopsphere.backend.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer>{

	List<Product> findByNameContainingIgnoreCase(String keyword);
}
