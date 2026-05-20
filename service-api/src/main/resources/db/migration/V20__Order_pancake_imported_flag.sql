-- Idempotency for Pancake POS → Thiyen order import (do not re-import unless force=true).

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS pancake_imported BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_orders_pancake_imported ON orders(pancake_imported);

UPDATE orders
SET pancake_imported = TRUE
WHERE pancake_order_id IS NOT NULL
  AND pancake_order_id <> ''
  AND (pancake_imported IS NULL OR pancake_imported = FALSE);
