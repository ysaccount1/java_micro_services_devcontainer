package com.example.shopping.service;

import com.example.shopping.model.Cart;
import com.example.shopping.model.CartItem;
import com.example.shopping.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import jakarta.annotation.PostConstruct;

@Service
public class ShoppingService {
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductService productService;

    @Transactional
    public Cart addToCart(Long userId, CartItem item) {
        // Check and update product stock first
        try {
            productService.updateProductStock(item.getProductId(), item.getQuantity());
        } catch (IllegalStateException e) {
            // Re-throw the detailed error message from ProductService
            throw e;
        }
        
        List<Cart> carts = cartRepository.findByUserId(userId);
        Cart cart;
        
        if (carts.isEmpty()) {
            cart = new Cart();
            cart.setUserId(userId);
        } else {
            // Use the first cart or merge carts if multiple exist
            cart = carts.get(0);
        }
        
        cart.getItems().add(item);
        // Don't set total here, it will be calculated on retrieval
        return cartRepository.save(cart);
    }

    public Cart getCart(Long userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);
        
        if (carts.isEmpty()) {
            Cart cart = new Cart();
            cart.setUserId(userId);
            return cartRepository.save(cart);
        } else {
            // Return the first cart found
            Cart cart = carts.get(0);
            // Calculate total on retrieval
            cart.setTotal(calculateTotal(cart));
            return cart;
        }
    }
    
    @Transactional
    public Cart updateCartItem(Long userId, Long itemId, Integer quantity) {
        Cart cart = getCart(userId);
        
        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
                
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            int quantityDifference = quantity - item.getQuantity();
            
            // If increasing quantity, check stock availability
            if (quantityDifference > 0) {
                try {
                    productService.updateProductStock(item.getProductId(), quantityDifference);
                } catch (IllegalStateException e) {
                    // Re-throw the detailed error message from ProductService
                    throw e;
                }
            } else if (quantityDifference < 0) {
                // If decreasing quantity, restore stock
                productService.restoreProductStock(item.getProductId(), Math.abs(quantityDifference));
            }
            
            item.setQuantity(quantity);
            // Don't set total here, it will be calculated on retrieval
            cart = cartRepository.save(cart);
            cart.setTotal(calculateTotal(cart));
            return cart;
        } else {
            throw new NoSuchElementException("Cart item not found");
        }
    }
    
    @Transactional
    public Cart removeFromCart(Long userId, Long itemId) {
        Cart cart = getCart(userId);
        
        Optional<CartItem> itemOpt = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst();
                
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            // Restore stock when removing item from cart
            productService.restoreProductStock(item.getProductId(), item.getQuantity());
            
            cart.getItems().remove(item);
            // Don't set total here, it will be calculated on retrieval
            cart = cartRepository.save(cart);
            cart.setTotal(calculateTotal(cart));
            return cart;
        } else {
            throw new NoSuchElementException("Cart item not found");
        }
    }

    private Double calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
    
    @PostConstruct
    @Transactional
    public void cleanupDuplicateCarts() {
        try {
            // Group carts by userId and keep only the first one for each user
            List<Long> userIds = cartRepository.findAll().stream()
                    .map(Cart::getUserId)
                    .distinct()
                    .toList();
                    
            for (Long userId : userIds) {
                List<Cart> userCarts = cartRepository.findByUserId(userId);
                if (userCarts.size() > 1) {
                    // Keep the first cart, delete the rest
                    Cart primaryCart = userCarts.get(0);
                    for (int i = 1; i < userCarts.size(); i++) {
                        cartRepository.delete(userCarts.get(i));
                    }
                }
            }
        } catch (Exception e) {
            // Log error but don't fail application startup
            System.err.println("Error cleaning up duplicate carts: " + e.getMessage());
        }
    }
}