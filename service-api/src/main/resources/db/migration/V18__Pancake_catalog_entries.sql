-- Local mirror of Pancake POS catalog (products + variations) for mapping Thiyen <-> Pancake <-> MeInvoice.

CREATE TABLE IF NOT EXISTS pancake_catalog_entry (
    id BIGSERIAL PRIMARY KEY,
    shop_id VARCHAR(50) NOT NULL,
    pancake_product_id VARCHAR(100) NOT NULL,
    pancake_variation_id VARCHAR(100) NOT NULL DEFAULT '',
    name VARCHAR(500),
    sku VARCHAR(200),
    price NUMERIC(19, 2),
    category VARCHAR(200),
    active BOOLEAN DEFAULT TRUE,
    raw_payload TEXT,
    local_product_id BIGINT,
    fetched_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pancake_catalog_local_product FOREIGN KEY (local_product_id) REFERENCES products(id) ON DELETE SET NULL,
    CONSTRAINT uq_pancake_catalog_shop_product_variation UNIQUE (shop_id, pancake_product_id, pancake_variation_id)
);

CREATE INDEX IF NOT EXISTS idx_pancake_catalog_local_product ON pancake_catalog_entry(local_product_id);
CREATE INDEX IF NOT EXISTS idx_pancake_catalog_pancake_product ON pancake_catalog_entry(pancake_product_id);
CREATE INDEX IF NOT EXISTS idx_pancake_catalog_pancake_variation ON pancake_catalog_entry(pancake_variation_id);
CREATE INDEX IF NOT EXISTS idx_pancake_catalog_sku ON pancake_catalog_entry(sku);
