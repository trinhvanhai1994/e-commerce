-- Add priority field to products table
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS priority INTEGER DEFAULT 999;

-- Update existing products to have default priority (999 = lowest priority)
UPDATE products 
SET priority = 999 
WHERE priority IS NULL;

-- Create index for better query performance when sorting by priority
CREATE INDEX IF NOT EXISTS idx_products_priority ON products(priority);

