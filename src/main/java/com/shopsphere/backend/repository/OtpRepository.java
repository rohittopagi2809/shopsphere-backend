package com.shopsphere.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.shopsphere.backend.entity.Otp;


public interface OtpRepository extends JpaRepository<Otp, Integer> {
	Otp findByEmail(String email);
	
	@Transactional
	@Modifying
	@Query("DELETE FROM Otp o WHERE o.email = :email")
	void deleteByEmail(String email);
}
