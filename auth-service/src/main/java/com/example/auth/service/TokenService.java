package com.example.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private final StringRedisTemplate stringRedisTemplate;
    private final long tokenExpiration;
    private final String jwtSecret;
    
    public TokenService(StringRedisTemplate stringRedisTemplate,
                       @Value("${jwt.expiration}") long tokenExpiration,
                       @Value("${jwt.secret:defaultSecretKeyForDevelopmentEnvironmentOnly}") String jwtSecret) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.tokenExpiration = tokenExpiration;
        this.jwtSecret = jwtSecret;
    }
    
    private Key getSigningKey() {
        // Generate a secure key for HS512 algorithm
        if (jwtSecret.length() < 64) {
            // If the provided secret is not secure enough, generate a secure key
            return Keys.secretKeyFor(SignatureAlgorithm.HS512);
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    private static final String TOKEN_PREFIX = "auth:token:";
    private static final String USER_TOKEN_PREFIX = "auth:user:";
    
    /**
     * Generate a new JWT token for a user and store it in Redis
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenExpiration);
        
        // Create JWT token
        String token = Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
        
        try {
            // Store token with user ID as value
            stringRedisTemplate.opsForValue().set(TOKEN_PREFIX + token, userId.toString(), tokenExpiration, TimeUnit.MILLISECONDS);
            
            // Store user ID with token for easy invalidation
            stringRedisTemplate.opsForValue().set(USER_TOKEN_PREFIX + userId, token, tokenExpiration, TimeUnit.MILLISECONDS);
        } catch (RedisConnectionFailureException e) {
            // If Redis is unavailable, just return the token without storing
            System.err.println("Redis connection failed: " + e.getMessage());
        }
        
        return token;
    }
    
    /**
     * Validate if a token exists and is valid
     */
    public Long validateToken(String token) {
        try {
            // First check if token is in Redis
            String userId = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + token);
            if (userId != null) {
                return Long.valueOf(userId);
            }
            
            // If not in Redis (maybe Redis was down when token was created), validate JWT
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                
                // Check if token is expired
                if (claims.getExpiration().before(new Date())) {
                    return null;
                }
                
                return Long.valueOf(claims.getSubject());
            } catch (Exception e) {
                // Invalid JWT token
                return null;
            }
        } catch (RedisConnectionFailureException e) {
            System.err.println("Redis connection failed: " + e.getMessage());
            
            // Try to validate JWT directly
            try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                
                if (claims.getExpiration().before(new Date())) {
                    return null;
                }
                
                return Long.valueOf(claims.getSubject());
            } catch (Exception jwtException) {
                return null;
            }
        }
    }
    
    /**
     * Invalidate a user's token (logout)
     */
    public void invalidateToken(Long userId) {
        try {
            String token = stringRedisTemplate.opsForValue().get(USER_TOKEN_PREFIX + userId);
            if (token != null) {
                stringRedisTemplate.delete(TOKEN_PREFIX + token);
                stringRedisTemplate.delete(USER_TOKEN_PREFIX + userId);
            }
        } catch (RedisConnectionFailureException e) {
            System.err.println("Redis connection failed: " + e.getMessage());
        }
    }
    
    /**
     * Refresh a token's expiration time
     */
    public void refreshToken(String token) {
        try {
            String userId = stringRedisTemplate.opsForValue().get(TOKEN_PREFIX + token);
            if (userId != null) {
                stringRedisTemplate.expire(TOKEN_PREFIX + token, tokenExpiration, TimeUnit.MILLISECONDS);
                stringRedisTemplate.expire(USER_TOKEN_PREFIX + userId, tokenExpiration, TimeUnit.MILLISECONDS);
            }
        } catch (RedisConnectionFailureException e) {
            System.err.println("Redis connection failed: " + e.getMessage());
        }
    }
}