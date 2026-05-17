package com.shopsphere.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopsphere.backend.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	User findByEmail(String email);
}
