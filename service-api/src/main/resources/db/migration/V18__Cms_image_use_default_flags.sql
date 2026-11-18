-- CMS: separate packaged defaults from DB overrides (use_default + null path)

ALTER TABLE homepage_sections
    ADD COLUMN IF NOT EXISTS use_default_desktop BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS use_default_mobile BOOLEAN NOT NULL DEFAULT TRUE;

-- Packaged catalog paths that were previously seeded as content
-- Desktop: treat null/blank/packaged as use_default=true and clear path
UPDATE homepage_sections
SET use_default_desktop = CASE
        WHEN image_desktop IS NULL
            OR btrim(image_desktop) = ''
            OR image_desktop IN (
                '/images/logo/logo.png',
                '/images/banner.png',
                '/images/baner-second.png'
            ) THEN TRUE
        ELSE FALSE
    END,
    image_desktop = CASE
        WHEN image_desktop IS NULL
            OR btrim(image_desktop) = ''
            OR image_desktop IN (
                '/images/logo/logo.png',
                '/images/banner.png',
                '/images/baner-second.png'
            ) THEN NULL
        ELSE image_desktop
    END;

UPDATE homepage_sections
SET use_default_mobile = CASE
        WHEN image_mobile IS NULL
            OR btrim(image_mobile) = ''
            OR image_mobile IN (
                '/images/banner-mobile.png'
            ) THEN TRUE
        ELSE FALSE
    END,
    image_mobile = CASE
        WHEN image_mobile IS NULL
            OR btrim(image_mobile) = ''
            OR image_mobile IN (
                '/images/banner-mobile.png'
            ) THEN NULL
        ELSE image_mobile
    END;

-- quality.extra_json.items[].icon → useDefaultIcon + clear packaged
UPDATE homepage_sections
SET extra_json = COALESCE(
    (
        SELECT jsonb_build_object(
            'items',
            COALESCE(
                jsonb_agg(
                    CASE
                        WHEN elem->>'icon' IS NULL
                            OR btrim(COALESCE(elem->>'icon', '')) = ''
                            OR elem->>'icon' IN ('/images/iso.png', '/images/gmp.png')
                        THEN (elem - 'icon') || jsonb_build_object('icon', NULL, 'useDefaultIcon', TRUE)
                        ELSE elem || jsonb_build_object('useDefaultIcon', FALSE)
                    END
                    ORDER BY ord
                ),
                '[]'::jsonb
            )
        )
        FROM jsonb_array_elements(COALESCE(extra_json->'items', '[]'::jsonb)) WITH ORDINALITY AS t(elem, ord)
    ),
    '{"items":[]}'::jsonb
)
WHERE section_key = 'quality';

-- meal_gallery: banner + tiles (preserve title and other keys)
UPDATE homepage_sections
SET extra_json = extra_json || jsonb_build_object(
        'banner', CASE
            WHEN extra_json->>'banner' IS NULL
                OR btrim(COALESCE(extra_json->>'banner', '')) = ''
                OR extra_json->>'banner' = '/images/four-home.png'
            THEN NULL
            ELSE to_jsonb(extra_json->>'banner')
        END,
        'useDefaultBanner', CASE
            WHEN extra_json->>'banner' IS NULL
                OR btrim(COALESCE(extra_json->>'banner', '')) = ''
                OR extra_json->>'banner' = '/images/four-home.png'
            THEN TRUE
            ELSE FALSE
        END,
        'tiles', COALESCE(
            (
                SELECT jsonb_agg(
                    CASE
                        WHEN tile->>'src' IS NULL
                            OR btrim(COALESCE(tile->>'src', '')) = ''
                            OR tile->>'src' IN (
                                '/images/struct/1.png',
                                '/images/struct/2.png',
                                '/images/struct/3.png',
                                '/images/struct/4.png'
                            )
                        THEN (tile - 'src') || jsonb_build_object('src', NULL, 'useDefault', TRUE)
                        ELSE tile || jsonb_build_object('useDefault', FALSE)
                    END
                    ORDER BY ord
                )
                FROM jsonb_array_elements(COALESCE(extra_json->'tiles', '[]'::jsonb)) WITH ORDINALITY AS t(tile, ord)
            ),
            '[]'::jsonb
        )
    )
WHERE section_key = 'meal_gallery';
