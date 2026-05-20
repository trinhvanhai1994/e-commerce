package com.dragun.ecommerce.integration.pancake;

/**
 * Pancake POS integration literals (sync direction, API paths, log codes).
 */
public final class PancakeIntegrationConstants {

    public static final String DEFAULT_API_BASE_URL = "https://pos.pages.fm/api/v1";

    public static final String SYNC_DIRECTION_FROM_PANCAKE = "FROM_PANCAKE";
    public static final String SYNC_DIRECTION_TO_PANCAKE = "TO_PANCAKE";
    public static final String SYNC_DIRECTION_BIDIRECTIONAL = "BIDIRECTIONAL";

    public static final String ORDER_TYPE_PANCAKE = "PANCAKE";

    public static final String SYNC_ENTITY_ORDER = "ORDER";
    public static final String SYNC_ENTITY_PRODUCT = "PRODUCT";

    public static final String SYNC_LOG_STATUS_SUCCESS = "SUCCESS";
    public static final String SYNC_LOG_STATUS_FAILED = "FAILED";
    public static final String SYNC_LOG_STATUS_SKIPPED = "SKIPPED";

    public static final String SYNC_SKIP_REASON_ALREADY_IMPORTED = "pancake_imported=true";

    public static final String API_QUERY_PARAM_API_KEY = "api_key";
    public static final String API_QUERY_PARAM_PAGE_SIZE = "page_size";
    public static final int API_DEFAULT_PAGE_SIZE_PRODUCTS = 200;
    public static final int API_DEFAULT_PAGE_SIZE_ORDERS = 100;

    public static final String API_PATH_SHOPS = "/shops";
    public static final String API_PATH_SHOP_PRODUCTS = "/shops/{shopId}/products";
    public static final String API_PATH_SHOP_PRODUCT_BY_ID = "/shops/{shopId}/products/{id}";
    public static final String API_PATH_SHOP_ORDERS = "/shops/{shopId}/orders";
    public static final String API_PATH_SHOP_ORDER_BY_ID = "/shops/{shopId}/orders/{id}";

    private PancakeIntegrationConstants() {
    }

    public static boolean isSyncFromPancakeEnabled(String configuredDirection) {
        return SYNC_DIRECTION_FROM_PANCAKE.equals(configuredDirection)
                || SYNC_DIRECTION_BIDIRECTIONAL.equals(configuredDirection);
    }

    public static boolean isSyncToPancakeEnabled(String configuredDirection) {
        return SYNC_DIRECTION_TO_PANCAKE.equals(configuredDirection)
                || SYNC_DIRECTION_BIDIRECTIONAL.equals(configuredDirection);
    }

    public static boolean includesFromPancake(String directionParam) {
        return SYNC_DIRECTION_FROM_PANCAKE.equals(directionParam)
                || SYNC_DIRECTION_BIDIRECTIONAL.equals(directionParam);
    }

    public static boolean includesToPancake(String directionParam) {
        return SYNC_DIRECTION_TO_PANCAKE.equals(directionParam)
                || SYNC_DIRECTION_BIDIRECTIONAL.equals(directionParam);
    }
}
