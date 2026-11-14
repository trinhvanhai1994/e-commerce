-- CMS: one sections table for several public pages (home, story, philosophy).
-- Existing rows belong to the homepage; section_key becomes unique per page.

ALTER TABLE homepage_sections
    ADD COLUMN IF NOT EXISTS page_key VARCHAR(32) NOT NULL DEFAULT 'home';

ALTER TABLE homepage_sections
    DROP CONSTRAINT IF EXISTS homepage_sections_section_key_key;

CREATE UNIQUE INDEX IF NOT EXISTS uq_homepage_sections_page_section
    ON homepage_sections (page_key, section_key);

DROP INDEX IF EXISTS idx_homepage_sections_enabled;
CREATE INDEX IF NOT EXISTS idx_homepage_sections_page_sort
    ON homepage_sections (page_key, sort_order);
