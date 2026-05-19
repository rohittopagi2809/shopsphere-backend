package com.shopsphere.backend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shopsphere.backend.config.JwtUtil;
import com.shopsphere.backend.dto.AuthResponse;
import com.shopsphere.backend.entity.Otp;
import com.shopsphere.backend.entity.User;
import com.shopsphere.backend.repository.OtpRepository;
import com.shopsphere.backend.repository.UserRepository;
import com.shopsphere.backend.service.EmailService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private OtpRepository otpRepository;
	
	@Autowired
	private EmailService emailService;
	
	// Register
	@PostMapping("/register")
	public String registerUser(@RequestBody User user) {
		
		String email = user.getEmail().toLowerCase();
		user.setEmail(email);
		
		// Prevent Duplicate user
		if (userRepository.findByEmail(email) != null) {
			throw new RuntimeException("Email already exists");
		}
		
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setRole("USER");
		user.setVerified(true);
		user.setCreatedAt(LocalDateTime.now());
		
		userRepository.save(user);
		
		String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
		
		Otp otpEntity = new Otp(user.getEmail(), otp, LocalDateTime.now().plusMinutes(5));
		otpRepository.deleteByEmail(email);
		
		otpRepository.save(otpEntity);
//		emailService.sendOtp(email, otp);
		
		return "User Resgistered Successfully";
	}
	// =========
	// LOGIN
	// =========
	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@RequestBody User user) {
		String email = user.getEmail().toLowerCase();
		
		User existingUser = userRepository.findByEmail(user.getEmail());
		
		if (existingUser == null) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "User not found. Please register first"));
		}
		
		// OTP verification check
		if (!existingUser.isVerified()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Please verify your account first"));
		}
		
		if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Invalid password"));
		}
		
		// Generate Token with Role
		String token = JwtUtil.generateToken(existingUser.getEmail(), existingUser.getRole());
		
		return ResponseEntity.ok(new AuthResponse(token, "Login Successful"));
	}
	
	// ==============
	// VERIFY OTP
	// ==============
	@PostMapping("/verify")
	public String verifyOtp(@RequestParam String email, @RequestParam String otp) {
		email = email.toLowerCase();

	    Otp saved = otpRepository.findByEmail(email);

	    if (saved == null || !saved.getOtp().equals(otp)) {
	        throw new RuntimeException("Invalid OTP");
	    }

	    if (saved.getExpiry().isBefore(LocalDateTime.now())) {
	        throw new RuntimeException("OTP expired");
	    }

	    User user = userRepository.findByEmail(email);
	    user.setVerified(true);
	    userRepository.save(user);
	    
	    otpRepository.deleteByEmail(email);

	    return "Verified!";
	}
	
	@PostMapping("/resend")
	public String resendOtp(@RequestParam String email) {
		String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
		Otp otpEntity = new Otp(email, otp, LocalDateTime.now().plusMinutes(5));
		otpRepository.save(otpEntity);
		emailService.sendOtp(email, otp);
		
		return "OTP resent";
	}
	
	@GetMapping("/me")
	public User getCurrentUser(HttpServletRequest request) {
		String email = (String) request.getAttribute("email");
		return userRepository.findByEmail(email);
	}
	
	@PutMapping("/profile")
	public User updateProfile(@RequestBody User updateUser, HttpServletRequest request) {
		String email = (String) request.getAttribute("email");
		User user = userRepository.findByEmail(email);
		
		if (updateUser.getName() != null) {
			user.setName(updateUser.getName());
		}
		
		if (updateUser.getPhoneNumber() != null) {
			user.setPhoneNumber(updateUser.getPhoneNumber());
		}
		
		return userRepository.save(user);
	}
}
