-- Idempotency flag: order already has a successful MeInvoice (MISA) draft/invoice.

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS meinvoice_invoiced BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS meinvoice_ref_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS meinvoice_invoiced_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_orders_meinvoice_invoiced ON orders(meinvoice_invoiced);

-- Backfill from successful submissions (if any exist before this migration).
UPDATE orders o
SET meinvoice_invoiced = TRUE,
    meinvoice_ref_id = s.ref_id,
    meinvoice_invoiced_at = COALESCE(s.created_at, CURRENT_TIMESTAMP)
FROM (
    SELECT DISTINCT ON (order_business_id)
        order_business_id,
        ref_id,
        created_at
    FROM meinvoice_submissions
    WHERE success = TRUE
    ORDER BY order_business_id, created_at DESC
) s
WHERE o.order_id = s.order_business_id
  AND (o.meinvoice_invoiced IS NULL OR o.meinvoice_invoiced = FALSE);
