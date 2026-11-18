-- Product categories managed by admin (task-004). Replaces the 3 categories hardcoded in the admin product forms
-- and the storefront filter. products.category keeps storing the category code as a plain string (no foreign key):
-- the product API and the Pancake POS sync stay unchanged, and a code unknown to this table shows as-is.

CREATE TABLE IF NOT EXISTS product_categories (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    sort_order  INTEGER      NOT NULL DEFAULT 0,
    visible     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_by  VARCHAR(255),
    CONSTRAINT uq_product_categories_code UNIQUE (code)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_product_categories_name_ci ON product_categories (LOWER(name));

INSERT INTO product_categories (code, name, sort_order) VALUES
    ('ME_DEN', 'Mè đen', 1),
    ('HONG_DAU', 'Hồng đậu', 2),
    ('COMBO', 'Combo', 3)
ON CONFLICT (code) DO NOTHING;
