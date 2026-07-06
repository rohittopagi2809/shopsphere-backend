package com.shopsphere.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopsphere.backend.entity.CartItem;

public interface CartRepository extends JpaRepository<CartItem, Integer> {
	
	List<CartItem> findByUserId(Integer userId);
	
	CartItem findByUserIdAndProduct_Id(Integer userId, Integer productId);
}
