package com.example.auth.service;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.SignupRequest;
import com.example.auth.dto.AuthResponse;
import com.example.auth.model.User;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RedisTemplate<String, User> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    
    public AuthService(UserRepository userRepository, TokenService tokenService, 
                      RedisTemplate<String, User> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    
    private static final String USER_CACHE_PREFIX = "user:";
    private static final String LOGIN_ATTEMPTS_PREFIX = "login:attempts:";
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCKOUT_TIME = 15 * 60; // 15 minutes in seconds

    public AuthResponse login(LoginRequest loginRequest) {
        try {
            // Get user from database
            User user = userRepository.findByUsername(loginRequest.getUsername());
            
            if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
                try {
                    // Try to clear failed login attempts
                    String ipKey = LOGIN_ATTEMPTS_PREFIX + loginRequest.getUsername();
                    stringRedisTemplate.delete(ipKey);
                    
                    // Cache user in Redis for faster authentication
                    redisTemplate.opsForValue().set(USER_CACHE_PREFIX + user.getId(), user);
                } catch (RedisConnectionFailureException e) {
                    // Ignore Redis errors
                }
                
                // Generate token using Redis
                String token = tokenService.generateToken(user.getId());
                return new AuthResponse("Login successful", token, user.getId());
            }
            
            try {
                // Try to increment failed login attempts
                String ipKey = LOGIN_ATTEMPTS_PREFIX + loginRequest.getUsername();
                String attemptsStr = stringRedisTemplate.opsForValue().get(ipKey);
                Integer attempts = attemptsStr != null ? Integer.parseInt(attemptsStr) : null;
                
                if (attempts == null) {
                    stringRedisTemplate.opsForValue().set(ipKey, "1", LOCKOUT_TIME, TimeUnit.SECONDS);
                } else {
                    stringRedisTemplate.opsForValue().increment(ipKey);
                }
            } catch (RedisConnectionFailureException e) {
                // Ignore Redis errors
            }
            
            return new AuthResponse("Login failed", null, null);
        } catch (Exception e) {
            return new AuthResponse("Login failed: " + e.getMessage(), null, null);
        }
    }
    // Add @Transactional to the signup method
    @Transactional
    public AuthResponse signup(SignupRequest signupRequest) {
        try {
            // Check if username already exists
            if (userRepository.existsByUsername(signupRequest.getUsername())) {
                return new AuthResponse("Username already exists", null, null);
            }

            User user = new User();
            user.setUsername(signupRequest.getUsername());
            user.setPassword(signupRequest.getPassword());
            user.setEmail(signupRequest.getEmail());
            // Add to AuthService.java in the signup method
            System.out.println("Saving user: " + signupRequest.getUsername());
            User savedUser = userRepository.save(user);
            System.out.println("User saved with ID: " + savedUser.getId());

            
            // Cache user in Redis for faster authentication
            try {
                redisTemplate.opsForValue().set(USER_CACHE_PREFIX + user.getId(), user);
            } catch (RedisConnectionFailureException e) {
                // Ignore Redis errors
            }
            
            // Generate token
            String token = tokenService.generateToken(user.getId());
            return new AuthResponse("Signup successful", token, user.getId());
        } catch (DataIntegrityViolationException e) {
            return new AuthResponse("Signup failed: Username or email already exists", null, null);
        } catch (Exception e) {
            return new AuthResponse("Signup failed: " + e.getMessage(), null, null);
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public void logout(Long userId) {
        tokenService.invalidateToken(userId);
    }
} 