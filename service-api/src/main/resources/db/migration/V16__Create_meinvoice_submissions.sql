CREATE TABLE meinvoice_submissions (
    id              BIGSERIAL PRIMARY KEY,
    ref_id          VARCHAR(64)  NOT NULL UNIQUE,
    order_business_id VARCHAR(128) NOT NULL,
    success         BOOLEAN      NOT NULL DEFAULT FALSE,
    last_error_code VARCHAR(128),
    last_message    TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meinvoice_submissions_order_id ON meinvoice_submissions (order_business_id);

CREATE UNIQUE INDEX uq_meinvoice_submission_order_success
    ON meinvoice_submissions (order_business_id)
    WHERE success = TRUE;
