package com.example.auth.controller;

import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.SignupRequest;
import com.example.auth.dto.AuthResponse;
import com.example.auth.model.User;
import com.example.auth.service.AuthService;
import com.example.auth.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true", maxAge = 3600)
@Tag(name = "Authentication", description = "Authentication API endpoints")
public class AuthController {
    
    private final AuthService authService;
    private final TokenService tokenService;
    
    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }
    
    @Operation(
        summary = "Login a user", 
        description = "Authenticates a user and returns a token",
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful login", 
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping(value = "/login", produces = "application/json", consumes = "application/json")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Register a new user", 
        description = "Creates a new user account",
        tags = {"Authentication"},
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = SignupRequest.class))
        )
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully", 
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Username already exists")
    })
    @PostMapping(value = "/signup", produces = "application/json", consumes = "application/json")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest signupRequest) {
        AuthResponse response = authService.signup(signupRequest);
        return ResponseEntity.ok(response);
    }
    
    @Operation(
        summary = "Logout a user", 
        description = "Invalidates the user's token",
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logged out successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid token")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No token provided"));
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = tokenService.validateToken(token);
            if (userId != null) {
                authService.logout(userId);
                return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
            }
        }
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid token"));
    }
    
    @Operation(
        summary = "Validate token", 
        description = "Checks if a token is valid and refreshes it",
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token validation result")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null) {
            return ResponseEntity.ok(Map.of("valid", false));
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            Long userId = tokenService.validateToken(token);
            if (userId != null) {
                tokenService.refreshToken(token);
                return ResponseEntity.ok(Map.of("valid", true, "userId", userId));
            }
        }
        return ResponseEntity.ok(Map.of("valid", false));
    }
    
    @Operation(
        summary = "Get all users", 
        description = "Returns a list of all users",
        tags = {"Authentication"},
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @GetMapping("/users")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<User>> getAllUsers(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(403).build();
        }
        
        String tokenValue = token.substring(7);
        Long userId = tokenService.validateToken(tokenValue);
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }
        
        List<User> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }
} 