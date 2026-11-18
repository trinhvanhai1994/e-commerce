-- Blog slug is unique among non-deleted posts only, matching BlogPostService (existsBySlugAndDeletedAtIsNull).
-- Before this, creating a post with the slug of a soft-deleted one passed the service check and failed on the
-- table-wide UNIQUE constraint with a 500.

ALTER TABLE blog_posts
    DROP CONSTRAINT IF EXISTS blog_posts_slug_key;

CREATE UNIQUE INDEX IF NOT EXISTS uq_blog_posts_slug_active
    ON blog_posts (slug)
    WHERE deleted_at IS NULL;
