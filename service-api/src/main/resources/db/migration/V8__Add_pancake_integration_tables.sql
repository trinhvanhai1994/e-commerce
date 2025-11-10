-- Add Pancake integration tables

-- Add Pancake columns to products table
ALTER TABLE products 
ADD COLUMN IF NOT EXISTS pancake_product_id VARCHAR(100),
ADD COLUMN IF NOT EXISTS pancake_synced_at TIMESTAMP;

-- Add Pancake columns to orders table
ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS pancake_order_id VARCHAR(100),
ADD COLUMN IF NOT EXISTS pancake_synced_at TIMESTAMP;

-- Create pancake_product_mapping table
CREATE TABLE IF NOT EXISTS pancake_product_mapping (
    id BIGSERIAL PRIMARY KEY,
    local_product_id BIGINT NOT NULL,
    pancake_product_id VARCHAR(100) NOT NULL,
    pancake_variation_id VARCHAR(100),
    last_synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (local_product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE(local_product_id, pancake_product_id)
);

-- Create pancake_order_mapping table
CREATE TABLE IF NOT EXISTS pancake_order_mapping (
    id BIGSERIAL PRIMARY KEY,
    local_order_id BIGINT NOT NULL,
    pancake_order_id VARCHAR(100) NOT NULL,
    last_synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (local_order_id) REFERENCES orders(id) ON DELETE CASCADE,
    UNIQUE(local_order_id, pancake_order_id)
);

-- Create pancake_sync_log table
CREATE TABLE IF NOT EXISTS pancake_sync_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL, -- PRODUCT, ORDER, CUSTOMER
    entity_id VARCHAR(100) NOT NULL,
    sync_direction VARCHAR(20) NOT NULL, -- TO_PANCAKE, FROM_PANCAKE
    status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, PARTIAL
    error_message TEXT,
    synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_pancake_product_mapping_local ON pancake_product_mapping(local_product_id);
CREATE INDEX IF NOT EXISTS idx_pancake_product_mapping_pancake ON pancake_product_mapping(pancake_product_id);
CREATE INDEX IF NOT EXISTS idx_pancake_order_mapping_local ON pancake_order_mapping(local_order_id);
CREATE INDEX IF NOT EXISTS idx_pancake_order_mapping_pancake ON pancake_order_mapping(pancake_order_id);
CREATE INDEX IF NOT EXISTS idx_pancake_sync_log_entity ON pancake_sync_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_pancake_sync_log_synced_at ON pancake_sync_log(synced_at);
CREATE INDEX IF NOT EXISTS idx_products_pancake_id ON products(pancake_product_id);
CREATE INDEX IF NOT EXISTS idx_orders_pancake_id ON orders(pancake_order_id);

