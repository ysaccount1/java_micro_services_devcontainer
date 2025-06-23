package com.example.shopping.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResetService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private StringRedisTemplate redisTemplate;
    
    @Transactional
    public void resetEnvironment() {
        try {
            // First ensure tables exist by checking and creating if needed
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS carts (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id BIGINT NOT NULL, " +
                    "total DECIMAL(10, 2) DEFAULT 0.0)");
                    
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS cart_items (" +
                    "id SERIAL PRIMARY KEY, " +
                    "cart_id BIGINT NOT NULL, " +
                    "product_id BIGINT NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "price DECIMAL(10, 2) NOT NULL, " +
                    "CONSTRAINT fk_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE)");
            
            // Now clear the tables
            jdbcTemplate.execute("TRUNCATE TABLE cart_items CASCADE");
            jdbcTemplate.execute("TRUNCATE TABLE carts CASCADE");
            
            // Reset product data
            jdbcTemplate.execute("TRUNCATE TABLE products RESTART IDENTITY CASCADE");
            
            // Re-run the data.sql script by executing each statement
            // In ResetService.java
            jdbcTemplate.execute("INSERT INTO products (name, description, price, image_url, stock) VALUES ('Laptop', 'High-performance laptop with 16GB RAM', 999.99, 'Laptop', 50)");
            jdbcTemplate.execute("INSERT INTO products (name, description, price, image_url, stock) VALUES ('Smartphone', 'Latest model with 128GB storage', 699.99, 'Smartphone', 100)");
            jdbcTemplate.execute("INSERT INTO products (name, description, price, image_url, stock) VALUES ('Headphones', 'Noise-cancelling wireless headphones', 199.99, 'Headphones', 100)");
            jdbcTemplate.execute("INSERT INTO products (name, description, price, image_url, stock) VALUES ('Tablet', '10-inch screen with 64GB storage', 349.99, 'Tablet', 30)");
            jdbcTemplate.execute("INSERT INTO products (name, description, price, image_url, stock) VALUES ('Smartwatch', 'Fitness tracking and notifications', 249.99, 'Watch', 45)");
            
            // Clear Redis cache
            redisTemplate.getConnectionFactory().getConnection().flushAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to reset environment: " + e.getMessage(), e);
        }
    }
}