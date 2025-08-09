package com.bingbank.accountservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        // Get JWT token from request
        String token = getJwtFromRequest(request);
        
        // Check if token exists and is valid
        if (StringUtils.hasText(token) && validateToken(token)) {
            // Get user id from token
            String userId = getUserIdFromJWT(token);
            
            // Create authentication token
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // Set user in Spring Security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // Also check X-Auth-Token header (might be set by gateway)
        String xAuthToken = request.getHeader("X-Auth-Token");
        if (StringUtils.hasText(xAuthToken)) {
            return xAuthToken;
        }
        
        return null;
    }
    
    private boolean validateToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            logger.error("JWT validation error: " + e.getMessage());
        }
        return false;
    }
    
    private String getUserIdFromJWT(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
            
        return claims.getSubject();
    }
}