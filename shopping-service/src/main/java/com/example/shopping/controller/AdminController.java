package com.example.shopping.controller;

import com.example.shopping.service.ResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true", maxAge = 3600)
public class AdminController {

    @Autowired
    private ResetService resetService;
    
    @Operation(summary = "Reset environment", description = "Resets product data, cart data, and clears Redis cache")
    @ApiResponse(responseCode = "200", description = "Environment reset successfully")
    @PostMapping("/reset")
    public ResponseEntity<String> resetEnvironment() {
        resetService.resetEnvironment();
        return ResponseEntity.ok("Environment reset successfully");
    }
}