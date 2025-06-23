package com.example.shopping.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.example.shopping.model.Cart;
import com.example.shopping.model.CartItem;
import com.example.shopping.model.Product;
import com.example.shopping.service.ProductCacheService;
import com.example.shopping.service.ProductService;
import com.example.shopping.service.ShoppingService;
import com.example.shopping.service.TokenValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/shopping")
@CrossOrigin(origins = {"http://localhost", "http://localhost:3000"}, allowCredentials = "true", maxAge = 3600)
@Tag(name = "Shopping Cart", description = "Shopping Cart API endpoints")
public class ShoppingController {
    
    @Autowired
    private ShoppingService shoppingService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductCacheService productCacheService;
    
    @Autowired
    private TokenValidationService tokenValidationService;
    
    @Operation(summary = "Get user's cart", description = "Retrieves the current shopping cart for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @GetMapping("/cart")
    public ResponseEntity<Cart> getCart(
            @Parameter(description = "User ID") @RequestHeader("userId") Long userId) {
        Cart cart = shoppingService.getCart(userId);
        return ResponseEntity.ok(cart);
    }
    
    @Operation(summary = "Add item to cart", description = "Adds a new item to the user's shopping cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "409", description = "Not enough stock available")
    })
    @PostMapping("/cart/add")
    public ResponseEntity<?> addToCart(
            @Parameter(description = "User ID") @RequestHeader("userId") Long userId,
            @Parameter(description = "Cart item details") @RequestBody CartItem cartItem) {
        try {
            Cart updatedCart = shoppingService.addToCart(userId, cartItem);
            return ResponseEntity.ok(updatedCart);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("status", "OUT_OF_STOCK");
            response.put("productId", cartItem.getProductId());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }
    
    @Operation(summary = "Update cart item", description = "Updates the quantity of an item in the cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item updated successfully"),
        @ApiResponse(responseCode = "404", description = "Item not found"),
        @ApiResponse(responseCode = "409", description = "Not enough stock available")
    })
    @PutMapping("/cart/update/{itemId}")
    public ResponseEntity<?> updateCartItem(
            @Parameter(description = "User ID") @RequestHeader("userId") Long userId,
            @Parameter(description = "Item ID") @PathVariable Long itemId,
            @Parameter(description = "New quantity") @RequestParam Integer quantity) {
        try {
            Cart updatedCart = shoppingService.updateCartItem(userId, itemId, quantity);
            return ResponseEntity.ok(updatedCart);
        } catch (IllegalStateException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("status", "OUT_OF_STOCK");
            response.put("itemId", itemId);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Remove item from cart", description = "Removes an item from the user's cart")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item removed successfully"),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @DeleteMapping("/cart/remove/{itemId}")
    public ResponseEntity<Cart> removeFromCart(
            @Parameter(description = "User ID") @RequestHeader("userId") Long userId,
            @Parameter(description = "Item ID") @PathVariable Long itemId) {
        try {
            Cart updatedCart = shoppingService.removeFromCart(userId, itemId);
            return ResponseEntity.ok(updatedCart);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(
        summary = "Get all products", 
        description = "Retrieves all available products",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestHeader(value = "Authorization", required = false) String token) {
        
         if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String tokenValue = token.substring(7);
        Long userId = tokenValidationService.validateToken(tokenValue);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }
    
    @Operation(
        summary = "Get product by ID", 
        description = "Retrieves a specific product by its ID",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String tokenValue = token.substring(7);
        Long userId = tokenValidationService.validateToken(tokenValue);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return productService.getProductById(id)
                .map(product -> {
                    try {
                        // Increment view count when product is viewed
                        productCacheService.incrementProductViews(id);
                    } catch (Exception e) {
                        // Log error but don't fail the request
                        System.err.println("Error incrementing product views: " + e.getMessage());
                    }
                    return ResponseEntity.ok(product);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(
        summary = "Get product stock", 
        description = "Retrieves the current stock level for a product",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/products/{id}/stock")
    public ResponseEntity<Map<String, Object>> getProductStock(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String tokenValue = token.substring(7);
        Long userId = tokenValidationService.validateToken(tokenValue);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (productService.getProductById(id).isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("productId", id);
            response.put("stock", productService.getProductStock(id));
            
            try {
                response.put("views", productCacheService.getProductViews(id));
            } catch (Exception e) {
                // If Redis is unavailable, set views to 0
                response.put("views", 0L);
                // Log error but don't fail the request
                System.err.println("Error getting product views: " + e.getMessage());
            }
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(
        summary = "Reset product cache", 
        description = "Clears and reinitializes the product cache from the database",
        security = { @SecurityRequirement(name = "bearerAuth") }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cache reset successfully"),
        @ApiResponse(responseCode = "403", description = "Unauthorized access")
    })
    @SecurityRequirement(name = "bearerAuth")
    @RequestMapping(value = "/cache/reset", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<Map<String, String>> resetCache(
            @RequestHeader(value = "Authorization", required = false) String token) {
        
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String tokenValue = token.substring(7);
        Long userId = tokenValidationService.validateToken(tokenValue);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        try {
            productService.resetCache();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product cache reset successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to reset cache: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}