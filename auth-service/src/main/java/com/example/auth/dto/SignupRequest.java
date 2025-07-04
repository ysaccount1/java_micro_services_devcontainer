package com.example.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class SignupRequest {
    @Schema(description = "Username for registration", example = "newuser123")
    private String username;
    
    @Schema(description = "Password for registration", example = "password123")
    private String password;
    
    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    public SignupRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}