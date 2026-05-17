package com.shopsphere.backend.dto;

public class AuthResponse {

	private String token;
	private String message;
	
	public AuthResponse(String token, String message) {
		this.token = token;
		this.message = message;
	}
	
	public String getToken() {
		return token;
	}
	
	public String geMessage() {
		return message;
	}
}
