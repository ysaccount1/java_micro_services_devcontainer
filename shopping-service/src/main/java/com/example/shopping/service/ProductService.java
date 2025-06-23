package com.example.shopping.service;

import com.example.shopping.model.Product;
import com.example.shopping.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import jakarta.annotation.PostConstruct;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductCacheService productCacheService;
    
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    /**
     * Get current stock for a product, checking cache first then database
     */
    public int getProductStock(Long productId) {
        // Try to get stock from cache first
        Integer cachedStock = productCacheService.getProductStock(productId);
        if (cachedStock != null) {
            return cachedStock;
        }
        
        // If not in cache, get from database
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()) {
            // Update cache with current stock value
            productCacheService.updateProductStock(productId, product.get().getStock());
            return product.get().getStock();
        }
        
        return 0;
    }
    
    /**
     * Update product stock in both cache and database
     * @throws IllegalStateException if product is out of stock
     */
    @Transactional
    public boolean updateProductStock(Long productId, int quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            int newStock = product.getStock() - quantity;
            
            // Prevent negative stock
            if (newStock < 0) {
                int currentStock = product.getStock();
                throw new IllegalStateException("Product '" + product.getName() + "' is out of stock. Available: " + currentStock + ", Requested: " + quantity);
            }
            
            // Update database
            product.setStock(newStock);
            productRepository.save(product);
            
            // Update cache
            productCacheService.updateProductStock(productId, newStock);
            return true;
        }
        return false;
    }
    
    /**
     * Restore product stock (when removing from cart)
     */
    @Transactional
    public void restoreProductStock(Long productId, int quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            int newStock = product.getStock() + quantity;
            
            // Update database
            product.setStock(newStock);
            productRepository.save(product);
            
            // Update cache
            productCacheService.updateProductStock(productId, newStock);
        }
    }
    
    // Initialize cache after application context is fully started
    @PostConstruct
    public void initializeCache() {
        try {
            // Initialize cache with all product stocks
            List<Product> products = productRepository.findAll();
            for (Product product : products) {
                productCacheService.updateProductStock(product.getId(), product.getStock());
            }
        } catch (Exception e) {
            // Log error but don't fail application startup
            System.err.println("Error initializing product cache: " + e.getMessage());
        }
    }
    
    /**
     * Reset and reinitialize the product cache from database
     */
    @Transactional(readOnly = true)
    public void resetCache() {
        // Clear existing cache
        productCacheService.clearProductStockCache();
        
        // Reinitialize from database
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            productCacheService.updateProductStock(product.getId(), product.getStock());
        }
    }
}