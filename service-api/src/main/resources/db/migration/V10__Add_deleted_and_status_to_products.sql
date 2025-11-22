-- Add deleted and status fields to products table
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';

-- Update existing products to have default values
UPDATE products 
SET deleted = FALSE, status = 'ACTIVE' 
WHERE deleted IS NULL OR status IS NULL;

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_products_deleted ON products(deleted);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);

