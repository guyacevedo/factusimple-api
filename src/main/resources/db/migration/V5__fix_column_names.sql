-- Fix column name mismatches in users table
-- V1 created products_count, customers_count, invoice_count
-- User entity maps to product_count, customer_count, invoice_count

ALTER TABLE users RENAME COLUMN products_count TO product_count;
ALTER TABLE users RENAME COLUMN customers_count TO customer_count;
