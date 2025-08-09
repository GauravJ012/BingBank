package com.bingbank.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${app.jwt-secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.public-paths}")
    private String publicPaths;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // Check if the path is in public paths list
        List<String> publicPathsList = Arrays.asList(publicPaths.split(","));
        for (String publicPath : publicPathsList) {
            if (path.trim().equals(publicPath.trim())) {
                return chain.filter(exchange);
            }
        }
        
        // Also allow OPTIONS requests (CORS preflight)
        if (request.getMethod().name().equals("OPTIONS")) {
            return chain.filter(exchange);
        }
        
        // Check for Authorization header
        if (!request.getHeaders().containsKey("Authorization")) {
            return onError(exchange, "No Authorization header", HttpStatus.UNAUTHORIZED);
        }
        
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
        }
        
        // Extract JWT token
        String token = authHeader.substring(7);
        
        try {
            // Validate token
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // Add user info to headers for downstream services
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", claims.getSubject())
                    .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            return onError(exchange, "Invalid JWT token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // High priority
    }
}