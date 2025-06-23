package com.example.shopping.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class ProductCacheService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    private static final String PRODUCT_VIEW_COUNT = "product:views:";
    private static final String PRODUCT_STOCK_KEY = "product:stock:";
    
    // Local cache as fallback when Redis is unavailable
    private final Map<String, String> localCache = new HashMap<>();
    
    /**
     * Increment product view count
     */
    public Long incrementProductViews(Long productId) {
        try {
            String key = PRODUCT_VIEW_COUNT + productId;
            Long views = stringRedisTemplate.opsForValue().increment(key);
            
            // Set expiry if it's a new key
            if (views != null && views == 1) {
                stringRedisTemplate.expire(key, 7, TimeUnit.DAYS);
            }
            
            return views;
        } catch (RedisConnectionFailureException e) {
            // Fallback to local cache
            String key = PRODUCT_VIEW_COUNT + productId;
            String currentValue = localCache.getOrDefault(key, "0");
            long newValue = Long.parseLong(currentValue) + 1;
            localCache.put(key, String.valueOf(newValue));
            return newValue;
        }
    }
    
    /**
     * Get product view count
     */
    public Long getProductViews(Long productId) {
        try {
            String value = stringRedisTemplate.opsForValue().get(PRODUCT_VIEW_COUNT + productId);
            return value != null ? Long.valueOf(value) : 0L;
        } catch (RedisConnectionFailureException e) {
            // Fallback to local cache
            String key = PRODUCT_VIEW_COUNT + productId;
            String value = localCache.getOrDefault(key, "0");
            return Long.valueOf(value);
        }
    }
    
    /**
     * Update product stock in cache
     */
    public void updateProductStock(Long productId, Integer stock) {
        try {
            stringRedisTemplate.opsForValue().set(PRODUCT_STOCK_KEY + productId, stock.toString(), 1, TimeUnit.HOURS);
        } catch (RedisConnectionFailureException e) {
            // Fallback to local cache
            localCache.put(PRODUCT_STOCK_KEY + productId, stock.toString());
        }
    }
    
    /**
     * Get product stock from cache
     */
    public Integer getProductStock(Long productId) {
        try {
            String stock = stringRedisTemplate.opsForValue().get(PRODUCT_STOCK_KEY + productId);
            return stock != null ? Integer.valueOf(stock) : null;
        } catch (RedisConnectionFailureException e) {
            // Fallback to local cache
            String stock = localCache.get(PRODUCT_STOCK_KEY + productId);
            return stock != null ? Integer.valueOf(stock) : null;
        }
    }
    
    /**
     * Clear all product stock cache entries
     */
    public void clearProductStockCache() {
        try {
            // Clear Redis cache
            Set<String> keys = stringRedisTemplate.keys(PRODUCT_STOCK_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
            
            // Clear view counts too
            keys = stringRedisTemplate.keys(PRODUCT_VIEW_COUNT + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (RedisConnectionFailureException e) {
            // Log error but continue
            System.err.println("Failed to clear Redis cache: " + e.getMessage());
        }
        
        // Clear local cache
        localCache.clear();
    }
}