-- Update product ID from 52 to 5 (if exists)
-- This migration is a fix for old data that might have product_id = 52
-- Note: V4 has been fixed to use product_id = 5 directly, so this migration
-- only runs if there's existing data with product_id = 52

-- Step 1: Update order_items table (if any orders reference product_id = 52)
UPDATE order_items 
SET product_id = 5 
WHERE product_id = 52;

-- Step 2: Delete product_images for old product_id (if exists)
DELETE FROM product_images WHERE product_id = 52;

-- Step 3: Update products table (if product with id = 52 exists)
-- Note: This will only update if the product exists
UPDATE products 
SET id = 5 
WHERE id = 52;

-- Step 4: Re-insert gallery images with new product_id (only if product_id = 5 doesn't have images yet)
-- This will only insert if the images don't already exist (from V4)
INSERT INTO product_images (product_id, image_url)
SELECT 5, image_url
FROM (
    SELECT '/images/products/details/mix/0.png' AS image_url
    UNION ALL SELECT '/images/products/details/mix/1.png'
    UNION ALL SELECT '/images/products/details/mix/2.png'
    UNION ALL SELECT '/images/products/details/mix/3.png'
    UNION ALL SELECT '/images/products/details/mix/4.png'
    UNION ALL SELECT '/images/products/details/mix/5.png'
    UNION ALL SELECT '/images/products/details/mix/6.png'
    UNION ALL SELECT '/images/products/details/mix/7.png'
    UNION ALL SELECT '/images/products/details/mix/8.png'
    UNION ALL SELECT '/images/products/details/mix/9.png'
    UNION ALL SELECT '/images/products/details/mix/10.png'
    UNION ALL SELECT '/images/products/details/mix/11.png'
) AS v
WHERE NOT EXISTS (
    SELECT 1 FROM product_images 
    WHERE product_id = 5 AND image_url = v.image_url
);

