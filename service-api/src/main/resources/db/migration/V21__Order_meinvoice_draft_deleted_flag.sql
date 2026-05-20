-- Draft deleted on MeInvoice (MISA) but RefID kept for PDF preview/download.
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS meinvoice_draft_deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_orders_meinvoice_draft_deleted ON orders(meinvoice_draft_deleted);
