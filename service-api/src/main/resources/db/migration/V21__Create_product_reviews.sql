-- Product reviews: customers submit (PENDING), admin approves/hides/edits; only APPROVED rows are public.
-- Replaces the 5 reviews hardcoded in ProductDetail.vue (same for every product) and the localStorage-only
-- customer reviews. Rating summary on the product page is computed from APPROVED rows.

CREATE TABLE IF NOT EXISTS product_reviews (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    author_name     VARCHAR(100) NOT NULL,
    rating          SMALLINT NOT NULL,
    content         TEXT NOT NULL,
    images          JSONB NOT NULL DEFAULT '[]'::jsonb,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    source          VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER',
    approved_at     TIMESTAMPTZ,
    approved_by     VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(255),
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT chk_review_status CHECK (status IN ('PENDING', 'APPROVED', 'HIDDEN')),
    CONSTRAINT chk_review_source CHECK (source IN ('CUSTOMER', 'ADMIN', 'SEED')),
    CONSTRAINT chk_review_images CHECK (jsonb_typeof(images) = 'array' AND jsonb_array_length(images) <= 4)
);

CREATE INDEX IF NOT EXISTS idx_product_reviews_product_status
    ON product_reviews (product_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_product_reviews_status_created
    ON product_reviews (status, created_at DESC);

-- Seed: the 5 reviews previously hardcoded in the storefront, copied to every existing product so the
-- product pages look exactly as before. Admin can then edit or remove them per product.
-- created_at is staggered so the public order (newest first) matches the old display order.
INSERT INTO product_reviews (product_id, author_name, rating, content, images, status, source,
                             approved_at, approved_by, created_at, updated_at, updated_by)
SELECT p.id, r.author_name, 5, r.content, r.images::jsonb, 'APPROVED', 'SEED',
       NOW(), 'seed', NOW() - (r.ord || ' minutes')::interval, NOW(), 'seed'
FROM products p
CROSS JOIN (VALUES
    (1, 'Thu Quyên',
     'Giao hàng nhanh, hàng chính hãng, đóng gói bọc xốp cẩn thận, Vị thơm mùi mè, pha nhanh, dễ pha, hợp với người bận rộn như mình.',
     '["/images/review/comment1/5f70f3785c7bd025896a1.jpg", "/images/review/comment1/cba003a9acaa20f479bb2.jpg"]'),
    (2, 'Mai Trân',
     'Mẫu mã và chất lượng OK ạ. Shop tư vấn nhiệt tình. Hàng đóng gói cẩn thận. Sẽ ủng hộ Shop lâu dài',
     '["/images/review/comment2/55604d6ae2696e3737789.jpg", "/images/review/comment2/c59aea924591c9cf908010.jpg"]'),
    (3, 'Lê Lan',
     'Shop giao hàng nhanh, đóng gói cẩn thận, bột mè đen mịn, dễ pha, vị mè đen thơm, dễ uống',
     '["/images/review/comment3/ee0af18a5f89d3d78a9814.jpg"]'),
    (4, 'Quyền Thu',
     'Bột ngũ hắc thơm mùi mè đen, vị ngon béo ngậy. Bé nhà m cũng thích, chiều đi học về cứ làm cốc là ấm bụng.',
     '["/images/review/comment4/fbcebb48154b9915c05a13.jpg"]'),
    (5, 'Lê Hà',
     'cũng dùng thử mấy loại rồi mà chỉ thấy toàn mùi đỗ đen, nay mới thấy loại này ngon và rõ mùi vị của mè đen. Nghe nói tốt lắm, thử dùng xem sao',
     '["/images/review/comment5/1f973fb291b11def44a018.jpg", "/images/review/comment5/f19088b626b5aaebf3a417.jpg"]')
) AS r(ord, author_name, content, images)
WHERE NOT EXISTS (SELECT 1 FROM product_reviews x WHERE x.product_id = p.id AND x.source = 'SEED');
