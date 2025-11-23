-- Add display_order column to product_images table to preserve image order
ALTER TABLE product_images 
ADD COLUMN IF NOT EXISTS display_order INTEGER DEFAULT 0;

-- Update existing records: set display_order based on image_url number extraction
-- For mix folder: extract number from filename (0.png -> 0, 1.png -> 1, etc.)
-- For black/pink folders: extract number from filename (1.png -> 1, 2.png -> 2, etc.)
UPDATE product_images
SET display_order = CASE
    -- Extract number from path like /images/products/details/mix/0.png
    WHEN image_url ~ '/mix/(\d+)\.(png|jpg|jpeg)$' THEN 
        CAST(SUBSTRING(image_url FROM '/mix/(\d+)\.(png|jpg|jpeg)$') AS INTEGER)
    -- Extract number from path like /images/products/details/black/1.png
    WHEN image_url ~ '/black/(\d+)\.(png|jpg|jpeg)$' THEN 
        CAST(SUBSTRING(image_url FROM '/black/(\d+)\.(png|jpg|jpeg)$') AS INTEGER)
    -- Extract number from path like /images/products/details/pink/1.png
    WHEN image_url ~ '/pink/(\d+)\.(png|jpg|jpeg)$' THEN 
        CAST(SUBSTRING(image_url FROM '/pink/(\d+)\.(png|jpg|jpeg)$') AS INTEGER)
    -- Default: use row number as order
    ELSE 0
END
WHERE display_order = 0 OR display_order IS NULL;

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_product_images_order 
ON product_images(product_id, display_order);

