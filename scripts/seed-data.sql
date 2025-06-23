-- Seed data for development environment

-- Clear existing data
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE products CASCADE;

-- Insert users
INSERT INTO users (id, username, email, password, created_at) VALUES
(1, 'admin', 'admin@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', NOW()),  -- password: admin123
(2, 'user1', 'user1@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', NOW()),  -- password: admin123
(3, 'user2', 'user2@example.com', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', NOW());  -- password: admin123

-- Insert products
INSERT INTO products (id, name, description, price, stock, created_at) VALUES
(1, 'Laptop', 'High-performance laptop with 16GB RAM', 1299.99, 10, NOW()),
(2, 'Smartphone', 'Latest model with 128GB storage', 799.99, 20, NOW()),
(3, 'Headphones', 'Noise-cancelling wireless headphones', 199.99, 30, NOW()),
(4, 'Tablet', '10-inch screen with 64GB storage', 399.99, 15, NOW()),
(5, 'Smartwatch', 'Fitness tracking and notifications', 249.99, 25, NOW());

-- Reset sequences
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('products_id_seq', (SELECT MAX(id) FROM products));

-- Confirm completion
SELECT 'Seed data loaded successfully!' AS message;