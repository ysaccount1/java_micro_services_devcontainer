-- Check if products table is empty before inserting data
INSERT INTO products (name, description, price, image_url, stock)
SELECT 'Laptop', 'High-performance laptop with 16GB RAM', 999.99, 'https://picsum.photos/200/300?item=1', 50
WHERE NOT EXISTS (SELECT 1 FROM products where name = 'Laptop');

INSERT INTO products (name, description, price, image_url, stock)
SELECT 'Smartphone', 'Latest model with 128GB storage', 699.99, 'https://picsum.photos/200/300?item=2', 100
WHERE NOT EXISTS (SELECT 1 FROM products where name = 'Smartphone');

INSERT INTO products (name, description, price, image_url, stock)
SELECT 'Headphones', 'Noise-cancelling wireless headphones', 199.99, 'https://picsum.photos/200/300?item=3', 0
WHERE NOT EXISTS (SELECT 1 FROM products where name = 'Headphones');

INSERT INTO products (name, description, price, image_url, stock)
SELECT 'Tablet', '10-inch screen with 64GB storage', 349.99, 'https://picsum.photos/200/300?item=4', 30
WHERE NOT EXISTS (SELECT 1 FROM products where name = 'Tablet');

INSERT INTO products (name, description, price, image_url, stock)
SELECT 'Smartwatch', 'Fitness tracking and notifications', 249.99, 'https://picsum.photos/200/300?item=5', 45
WHERE NOT EXISTS (SELECT 1 FROM products where name = 'Smartwatch');

-- Update stock for testing out-of-stock functionality
UPDATE products SET stock = 0 WHERE name = 'Headphones';