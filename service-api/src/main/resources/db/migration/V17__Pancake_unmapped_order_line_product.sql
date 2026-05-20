-- Placeholder product for Pancake POS order lines when no local product matches pancake_product_id / variation.
-- Required because order_items.product_id is NOT NULL.

INSERT INTO products (
    name,
    price,
    stock,
    category,
    quantity,
    deleted,
    status,
    priority,
    pancake_product_id,
    created_at,
    updated_at
)
SELECT
    'POS: Hàng chưa map catalogue',
    0,
    0,
    'SYSTEM',
    '0',
    TRUE,
    'ACTIVE',
    99999,
    '__PANCAKE_ORDER_LINE_UNMAPPED__',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE pancake_product_id = '__PANCAKE_ORDER_LINE_UNMAPPED__'
);
