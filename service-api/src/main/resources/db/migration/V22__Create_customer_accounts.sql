-- Customer accounts (task-003, phase 1): storefront customers log in with their phone number.
-- Separate from `users` (admin only) so a customer token can never carry ROLE_ADMIN.
-- No OTP in this version (R6): phone_verified_at stays NULL until an OTP flow exists.

CREATE TABLE IF NOT EXISTS customers (
    id                    BIGSERIAL PRIMARY KEY,
    phone                 VARCHAR(10)  NOT NULL UNIQUE,  -- normalized: 0 + 9 digits
    password_hash         VARCHAR(100) NOT NULL,
    full_name             VARCHAR(100) NOT NULL,
    email                 VARCHAR(255),
    birth_date            DATE,
    gender                VARCHAR(10),
    -- default shipping address, used to prefill checkout
    address_recipient     VARCHAR(100),
    address_phone         VARCHAR(10),
    address_line          TEXT,
    province_code         VARCHAR(20),
    district_code         VARCHAR(20),
    ward_code             VARCHAR(20),
    status                VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    phone_verified_at     TIMESTAMPTZ,
    must_change_password  BOOLEAN      NOT NULL DEFAULT FALSE,
    -- bumped on password change / lock: access tokens carrying an older version are rejected
    token_version         INTEGER      NOT NULL DEFAULT 0,
    failed_login_count    INTEGER      NOT NULL DEFAULT 0,
    locked_until          TIMESTAMPTZ,
    consent_at            TIMESTAMPTZ  NOT NULL,
    consent_version       VARCHAR(20)  NOT NULL,
    last_login_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_customer_status CHECK (status IN ('ACTIVE', 'LOCKED', 'DELETED')),
    CONSTRAINT chk_customer_gender CHECK (gender IS NULL OR gender IN ('MALE', 'FEMALE', 'OTHER')),
    CONSTRAINT chk_customer_phone CHECK (phone ~ '^0[0-9]{9}$')
);

-- Refresh tokens are stored hashed (SHA-256 hex); revoked on logout, password change and rotation.
CREATE TABLE IF NOT EXISTS customer_refresh_tokens (
    id           BIGSERIAL PRIMARY KEY,
    customer_id  BIGINT      NOT NULL REFERENCES customers (id) ON DELETE CASCADE,
    token_hash   VARCHAR(64) NOT NULL UNIQUE,
    expires_at   TIMESTAMPTZ NOT NULL,
    revoked_at   TIMESTAMPTZ,
    user_agent   VARCHAR(255),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_customer_refresh_tokens_customer ON customer_refresh_tokens (customer_id);

-- Orders placed while logged in belong to the account (R7). Guest orders keep customer_id NULL.
ALTER TABLE orders ADD COLUMN IF NOT EXISTS customer_id BIGINT REFERENCES customers (id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders (customer_id, created_at DESC);

-- Reviews written by an account: one per product per account. Older reviews (guest, seed, admin) keep NULL.
ALTER TABLE product_reviews ADD COLUMN IF NOT EXISTS customer_id BIGINT REFERENCES customers (id) ON DELETE SET NULL;
CREATE UNIQUE INDEX IF NOT EXISTS ux_product_reviews_product_customer
    ON product_reviews (product_id, customer_id) WHERE customer_id IS NOT NULL;
