package com.dragun.ecommerce.cms;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * allowedMimeTypes = what the server accepts. The JDK has no WebP decoder, and the admin UI
 * (service-ui/src/constants/imageSlots.js) re-encodes every source to PNG/JPEG before upload, so WebP is
 * accepted as a source there but never sent here. Keep keys and sizes in sync with that file.
 */
@Getter
public enum ImageSlot {
    HERO_DESKTOP("hero_desktop", 21.0 / 9.0, 0.03, 1920, 823, 1600, 0, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    HERO_MOBILE("hero_mobile", 4.0 / 5.0, 0.03, 1080, 1350, 750, 0, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    STORY_SIDE("story_side", 4.0 / 5.0, 0.05, 800, 1000, 600, 0, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    QUALITY_ICON("quality_icon", 1.0, 0.02, 256, 256, 128, 128, 10 * 1024 * 1024, Set.of("image/png")),
    MEAL_BANNER("meal_banner", 16.0 / 9.0, 0.03, 1600, 900, 1200, 0, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    MEAL_TILE("meal_tile", 1.0, 0.01, 800, 800, 600, 600, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    BLOG_COVER("blog_cover", 16.0 / 10.0, 0.03, 1200, 750, 800, 0, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    BLOG_INLINE("blog_inline", 16.0 / 9.0, 0.05, 1600, 900, 600, 0, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    PRODUCT_MAIN("product_main", 1.0, 0.01, 1000, 1000, 600, 600, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    PRODUCT_GALLERY("product_gallery", 1.0, 0.01, 1000, 1000, 600, 600, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    SITE_LOGO("site_logo", 1.0, 0.05, 512, 512, 128, 128, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    // Story / philosophy pages: banner matches the existing /images/me/banner.jpg (2560x1030)
    PAGE_BANNER("page_banner", 2560.0 / 1030.0, 0.03, 1920, 773, 1280, 0, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    PAGE_FEATURE("page_feature", 1.0, 0.02, 1000, 1000, 600, 600, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png")),
    VALUE_ICON("value_icon", 1.0, 0.02, 512, 512, 256, 256, 10 * 1024 * 1024, Set.of("image/png")),
    // Product review photos (customer or admin). Square: the storefront shows them as square thumbnails.
    REVIEW_IMAGE("review_image", 1.0, 0.01, 800, 800, 300, 300, 10 * 1024 * 1024, Set.of("image/jpeg", "image/png"));

    private final String key;
    private final double aspectRatio;
    private final double aspectTolerance;
    private final int outputWidth;
    private final int outputHeight;
    private final int minWidth;
    private final int minHeight;
    private final long maxBytes;
    private final Set<String> allowedMimeTypes;

    ImageSlot(String key, double aspectRatio, double aspectTolerance, int outputWidth, int outputHeight,
              int minWidth, int minHeight, long maxBytes, Set<String> allowedMimeTypes) {
        this.key = key;
        this.aspectRatio = aspectRatio;
        this.aspectTolerance = aspectTolerance;
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxBytes = maxBytes;
        this.allowedMimeTypes = allowedMimeTypes;
    }

    public static ImageSlot fromKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("slot không được để trống");
        }
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
            .filter(s -> s.key.equals(normalized))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("slot không hợp lệ: " + key));
    }

    public boolean isAspectAllowed(int width, int height) {
        if (width <= 0 || height <= 0) {
            return false;
        }
        double actual = (double) width / (double) height;
        return Math.abs(actual - aspectRatio) <= aspectTolerance * aspectRatio
            || Math.abs(actual - aspectRatio) <= aspectTolerance;
    }

    public String folderPrefix() {
        if (key.startsWith("product_")) {
            return "products";
        }
        if (key.startsWith("blog_")) {
            return "cms/blogs";
        }
        if (key.startsWith("review_")) {
            return "reviews";
        }
        return "cms/" + key;
    }
}
