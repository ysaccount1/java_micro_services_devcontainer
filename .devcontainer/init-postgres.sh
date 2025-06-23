#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  CREATE DATABASE authdb;
  CREATE DATABASE shoppingdb;
EOSQL

# Connect to shoppingdb and create products table
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "shoppingdb" <<-EOSQL
  CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(255),
    stock INT NOT NULL DEFAULT 0
  );

  -- Insert sample products
  INSERT INTO products (name, description, price, image_url, stock) VALUES
    ('Laptop', 'High-performance laptop with 16GB RAM', 999.99, 'https://encrypted-tbn0.gstatic.com/shopping?q=tbn:ANd9GcS1ifsgWBpEk2XcKf3p9jGZLsIIm_7WvLmlvVyCGTWPN5A8TSGRjo_s0C2TxQq6IM3wUwAKB3x-QPkbuCnmtQlqcc-WzAXK6N6bzcpVRLHVIhlK-U2Yi62r8WZp1to-nTlNKP2ONQ&usqp=CAc', 50),
    ('Smartphone', 'Latest model with 128GB storage', 699.99, 'https://encrypted-tbn2.gstatic.com/shopping?q=tbn:ANd9GcSKhTWQqTo5kdRhtSFJlGLsA2rtksJ-Z-ZuCcrMo0eNGZmV6KKId6z87S7nPiUI0OkwPT5ZX-e8rycVWAkBM9UaXPBBqOduxTjqurOsh5r7f-j94ttescR3T_KkuhSDeLX0_FZTIB0&usqp=CAc', 100),
    ('Headphones', 'Noise-cancelling wireless headphones', 199.99, 'https://encrypted-tbn0.gstatic.com/shopping?q=tbn:ANd9GcS1ifsgWBpEk2XcKf3p9jGZLsIIm_7WvLmlvVyCGTWPN5A8TSGRjo_s0C2TxQq6IM3wUwAKB3x-QPkbuCnmtQlqcc-WzAXK6N6bzcpVRLHVIhlK-U2Yi62r8WZp1to-nTlNKP2ONQ&usqp=CAc', 75),
    ('Tablet', '10-inch screen with 64GB storage', 349.99, 'https://encrypted-tbn2.gstatic.com/shopping?q=tbn:ANd9GcSKhTWQqTo5kdRhtSFJlGLsA2rtksJ-Z-ZuCcrMo0eNGZmV6KKId6z87S7nPiUI0OkwPT5ZX-e8rycVWAkBM9UaXPBBqOduxTjqurOsh5r7f-j94ttescR3T_KkuhSDeLX0_FZTIB0&usqp=CAc', 30),
    ('Smartwatch', 'Fitness tracking and notifications', 249.99, 'https://encrypted-tbn2.gstatic.com/shopping?q=tbn:ANd9GcSKhTWQqTo5kdRhtSFJlGLsA2rtksJ-Z-ZuCcrMo0eNGZmV6KKId6z87S7nPiUI0OkwPT5ZX-e8rycVWAkBM9UaXPBBqOduxTjqurOsh5r7f-j94ttescR3T_KkuhSDeLX0_FZTIB0&usqp=CAc', 45);
EOSQL
