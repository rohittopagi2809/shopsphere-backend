package com.shopsphere.backend.config;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter{
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
			throws ServletException, IOException{
		
		if(request.getMethod().equalsIgnoreCase("OPTIONS")) {
			filterChain.doFilter(request, response);
			return;
		}
		
		String header = request.getHeader("Authorization");
		
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			
			try {
				String email = JwtUtil.extractEmail(token);
				String role = JwtUtil.extractRole(token);
				
				request.setAttribute("email", email);
				request.setAttribute("role", role);
				
				List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
				
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, 
						authorities);
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}
		filterChain.doFilter(request, response);
	}
	
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		
		return path.equals("/api/auth/login")
			|| path.equals("/api/auth/register")
			|| path.equals("/api/auth/verify")
			|| path.equals("/api/auth/resend")
			||
			
		request.getMethod().equalsIgnoreCase("OPTIONS");
	}
	
}
