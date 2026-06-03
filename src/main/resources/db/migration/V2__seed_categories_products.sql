-- V2__seed_categories_products.sql
-- Seed initial categories and sample products

-- Insert categories and capture their IDs
INSERT INTO categories (name, description, status)
VALUES ('Electronics', 'Electronic gadgets and accessories', 'ACTIVE');
SET @cat_electronics = LAST_INSERT_ID();

INSERT INTO categories (name, description, status)
VALUES ('Books', 'Books and magazines', 'ACTIVE');
SET @cat_books = LAST_INSERT_ID();

INSERT INTO categories (name, description, status)
VALUES ('Clothing', 'Men and Women Apparel', 'ACTIVE');
SET @cat_clothing = LAST_INSERT_ID();

-- Insert sample products referencing the category IDs
INSERT INTO products (category_id, name, description, price, stock, image_url, status)
VALUES
(@cat_electronics, 'Smartphone XYZ', 'A modern smartphone with great battery life.', 499.99, 50, 'https://example.com/images/smartphone.jpg', 'ACTIVE'),
(@cat_books, 'Clean Code', 'A Handbook of Agile Software Craftsmanship by Robert C. Martin', 29.90, 100, 'https://example.com/images/cleancode.jpg', 'ACTIVE'),
(@cat_clothing, 'Basic T-Shirt', '100% cotton t-shirt', 9.99, 200, 'https://example.com/images/tshirt.jpg', 'ACTIVE');
