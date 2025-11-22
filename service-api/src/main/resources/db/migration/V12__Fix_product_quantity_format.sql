-- Fix product quantity format from "500g/lon" to "1 lon x 500g" and "500g/lon x 2" to "2 lon x 500g"
-- This migration updates the quantity field format to match the expected format in the admin panel

-- Update Product 1: BỘT NGŨ HẮC MÈ ĐEN
UPDATE products 
SET quantity = '1 lon x 500g' 
WHERE id = 1 AND quantity = '500g/lon';

-- Update Product 2: COMBO 2 LON BỘT NGŨ HẮC MÈ ĐEN
UPDATE products 
SET quantity = '2 lon x 500g' 
WHERE id = 2 AND quantity = '500g/lon x 2';

-- Update Product 3: BỘT NGŨ SẮC HỒNG ĐẬU
UPDATE products 
SET quantity = '1 lon x 500g' 
WHERE id = 3 AND quantity = '500g/lon';

-- Update Product 4: COMBO 2 LON BỘT NGŨ SẮC HỒNG ĐẬU
UPDATE products 
SET quantity = '2 lon x 500g' 
WHERE id = 4 AND quantity = '500g/lon x 2';

-- Update Product 5: COMBO 2 (1 BỘT NGŨ HẮC MÈ ĐEN + 1 BỘT NGŨ SẮC HỒNG ĐẬU)
UPDATE products 
SET quantity = '2 lon x 500g' 
WHERE id = 5 AND quantity = '500g/lon x 2';

-- Also handle any other products that might have the old format
UPDATE products 
SET quantity = '1 lon x 500g' 
WHERE quantity = '500g/lon' AND quantity != '1 lon x 500g';

UPDATE products 
SET quantity = '2 lon x 500g' 
WHERE quantity = '500g/lon x 2' AND quantity != '2 lon x 500g';

