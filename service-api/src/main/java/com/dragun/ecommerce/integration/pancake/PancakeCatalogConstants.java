package com.dragun.ecommerce.integration.pancake;

import com.dragun.ecommerce.model.entity.Product;

/**
 * Catalog identifiers used by Pancake ↔ DB sync (see Flyway V17).
 */
public final class PancakeCatalogConstants {

    /**
     * {@code products.pancake_product_id} for placeholder rows when a POS line has no matching product.
     */
    public static final String UNMAPPED_LINE_PANCAKE_PRODUCT_ID = "__PANCAKE_ORDER_LINE_UNMAPPED__";

    public static final String INTERNAL_PANCAKE_PRODUCT_ID_PREFIX = "__PANCAKE_";

    public static final String SYSTEM_CATEGORY = "SYSTEM";

    public static boolean isSystemPancakeProductId(String pancakeProductId) {
        if (pancakeProductId == null || pancakeProductId.isBlank()) {
            return false;
        }
        return UNMAPPED_LINE_PANCAKE_PRODUCT_ID.equals(pancakeProductId)
                || pancakeProductId.startsWith(INTERNAL_PANCAKE_PRODUCT_ID_PREFIX);
    }

    public static boolean isSyncableToPancake(Product product) {
        if (product == null) {
            return false;
        }
        if (Boolean.TRUE.equals(product.getDeleted())) {
            return false;
        }
        if (SYSTEM_CATEGORY.equalsIgnoreCase(product.getCategory())) {
            return false;
        }
        return !isSystemPancakeProductId(product.getPancakeProductId());
    }

    private PancakeCatalogConstants() {
    }
}
