package com.shopsphere.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopsphere.backend.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
	
	List<Order> findByUserId(Integer userId);
}
