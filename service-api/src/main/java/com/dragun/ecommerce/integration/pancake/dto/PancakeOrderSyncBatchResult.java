package com.dragun.ecommerce.integration.pancake.dto;

/**
 * Result of a batch import from Pancake POS.
 */
public record PancakeOrderSyncBatchResult(int imported, int skipped, int failed) {
}
