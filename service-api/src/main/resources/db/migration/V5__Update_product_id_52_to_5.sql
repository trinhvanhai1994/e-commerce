-- Update product ID from 52 to 5
-- This migration updates the product ID to maintain sequential numbering
-- Note: This handles foreign key constraints by updating order_items first

-- Step 1: Update order_items table (if any orders reference product_id = 52)
UPDATE order_items 
SET product_id = 5 
WHERE product_id = 52;

-- Step 2: Delete product_images for old product_id
DELETE FROM product_images WHERE product_id = 52;

-- Step 3: Update products table
UPDATE products 
SET id = 5 
WHERE id = 52;

-- Step 4: Re-insert gallery images with new product_id
INSERT INTO product_images (product_id, image_url) VALUES
(5, '/images/products/details/mix/0.png'),
(5, '/images/products/details/mix/1.png'),
(5, '/images/products/details/mix/2.png'),
(5, '/images/products/details/mix/3.png'),
(5, '/images/products/details/mix/4.png'),
(5, '/images/products/details/mix/5.png'),
(5, '/images/products/details/mix/6.png'),
(5, '/images/products/details/mix/7.png'),
(5, '/images/products/details/mix/8.png'),
(5, '/images/products/details/mix/9.png'),
(5, '/images/products/details/mix/10.png'),
(5, '/images/products/details/mix/11.png');

