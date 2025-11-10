-- Extend product fields length to support longer values
-- Fix: value too long for type character varying(500)

-- Extend main_image to TEXT to support long URLs or base64 images
ALTER TABLE products 
ALTER COLUMN main_image TYPE TEXT;

-- Extend name to support longer product names
ALTER TABLE products 
ALTER COLUMN name TYPE VARCHAR(1000);

-- Extend category to support longer category names
ALTER TABLE products 
ALTER COLUMN category TYPE VARCHAR(255);

-- Extend quantity to support longer quantity descriptions
ALTER TABLE products 
ALTER COLUMN quantity TYPE VARCHAR(255);

-- Extend product_images image_url to TEXT for long URLs
ALTER TABLE product_images 
ALTER COLUMN image_url TYPE TEXT;

