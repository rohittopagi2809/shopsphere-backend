package com.shopsphere.backend.config;

import java.util.Date;
import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.*;

public class JwtUtil {
	
	private static final String SECRET = "mysecretkeymysecretkeymysecretkey123";
	private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
	
	public static String generateToken(String email, String role) {
		
		return Jwts.builder()
				.setSubject(email)
				.claim("role", role)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}
	
	public static String extractEmail(String token) {
		
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}
	
	public static String extractRole(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.get("role", String.class);
	}
}
