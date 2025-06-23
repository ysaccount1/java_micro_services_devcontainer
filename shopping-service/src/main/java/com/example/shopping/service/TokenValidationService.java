package com.example.shopping.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TokenValidationService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${auth.service.url}")
    private String authServiceUrl;
    
    private static final String TOKEN_PREFIX = "auth:token:";
    
    /**
     * Validate a token by checking Redis first, then falling back to auth service API
     */
    public Long validateToken(String token) {
        // For debugging
        System.out.println("Validating token: " + token);
        
        try {
            // First try to validate from Redis directly
            String userId = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + token);
            
            if (userId != null) {
                System.out.println("Token validated from Redis: userId=" + userId);
                return Long.valueOf(userId);
            }
            
            System.out.println("Token not found in Redis, calling auth service at: " + authServiceUrl);
            
            // If not in Redis, call auth service with the token in the header
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                authServiceUrl + "/api/auth/validate",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            Map<String, Object> response = responseEntity.getBody();
            System.out.println("Auth service response: " + response);
            
            if (response != null && Boolean.TRUE.equals(response.get("valid"))) {
                Long validatedUserId = Long.valueOf(response.get("userId").toString());
                System.out.println("Token validated from auth service: userId=" + validatedUserId);
                return validatedUserId;
            }
        } catch (Exception e) {
            // Log error and continue
            System.err.println("Error validating token: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Token validation failed");
        return null;
    }
}