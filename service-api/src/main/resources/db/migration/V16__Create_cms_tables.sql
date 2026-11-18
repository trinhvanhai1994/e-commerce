-- CMS: homepage sections + blog posts

CREATE TABLE IF NOT EXISTS homepage_sections (
    id              BIGSERIAL PRIMARY KEY,
    section_key     VARCHAR(64) NOT NULL UNIQUE,
    title           TEXT,
    body            TEXT,
    cta_text        VARCHAR(255),
    cta_link        VARCHAR(500),
    image_desktop   VARCHAR(1000),
    image_mobile    VARCHAR(1000),
    extra_json      JSONB NOT NULL DEFAULT '{}'::jsonb,
    enabled         BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order      INT NOT NULL DEFAULT 0,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by      VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS blog_posts (
    id              BIGSERIAL PRIMARY KEY,
    slug            VARCHAR(120) NOT NULL UNIQUE,
    title           TEXT NOT NULL,
    excerpt         TEXT NOT NULL,
    cover_image     VARCHAR(1000),
    content_json    JSONB NOT NULL DEFAULT '[]'::jsonb,
    status          VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    show_on_home    BOOLEAN NOT NULL DEFAULT FALSE,
    home_order      INT,
    published_at    TIMESTAMPTZ,
    deleted_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    CONSTRAINT chk_blog_status CHECK (status IN ('DRAFT', 'PUBLISHED')),
    CONSTRAINT chk_blog_home_order CHECK (home_order IS NULL OR (home_order BETWEEN 1 AND 3))
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_blog_home_order_active
    ON blog_posts (home_order)
    WHERE show_on_home = TRUE AND deleted_at IS NULL AND home_order IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_blog_posts_status_published
    ON blog_posts (status, published_at DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_homepage_sections_enabled
    ON homepage_sections (enabled, sort_order);
