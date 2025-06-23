package com.example.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class AuthResponse {
    @Schema(description = "Response message", example = "Login successful")
    private String message;
    
    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    
    @Schema(description = "User ID", example = "1")
    private Long userId;

    public AuthResponse() {
    }

    public AuthResponse(String message, String token, Long userId) {
        this.message = message;
        this.token = token;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}