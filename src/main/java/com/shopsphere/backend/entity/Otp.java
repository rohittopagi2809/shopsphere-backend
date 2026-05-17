package com.shopsphere.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Otp {
	@Id
	@GeneratedValue
	private Integer id;
	@Column
	private String email;
	@Column
	private String otp;
	@Column
	private LocalDateTime expiry;
	
	public Otp() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Otp(String email, String otp, LocalDateTime expiry) {
		super();
		this.email = email;
		this.otp = otp;
		this.expiry = expiry;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public LocalDateTime getExpiry() {
		return expiry;
	}

	public void setExpiry(LocalDateTime expiry) {
		this.expiry = expiry;
	}
	
}
