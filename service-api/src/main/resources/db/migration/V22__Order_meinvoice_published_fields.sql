ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS meinvoice_published BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS meinvoice_published_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS meinvoice_transaction_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS meinvoice_inv_no VARCHAR(32),
    ADD COLUMN IF NOT EXISTS meinvoice_publish_error_code VARCHAR(128),
    ADD COLUMN IF NOT EXISTS meinvoice_send_tax_status INTEGER;

CREATE INDEX IF NOT EXISTS idx_orders_meinvoice_published ON orders(meinvoice_published);
