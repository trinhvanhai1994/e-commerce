package com.dragun.ecommerce.integration.pancake.service;

import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.PancakeCatalogConstants;
import com.dragun.ecommerce.integration.pancake.PancakeIntegrationConstants;
import com.dragun.ecommerce.integration.pancake.client.PancakeApiClient;
import com.dragun.ecommerce.integration.pancake.dto.PancakeOrderDto;
import com.dragun.ecommerce.integration.pancake.dto.PancakeOrderSyncBatchResult;
import com.dragun.ecommerce.integration.pancake.mapper.OrderMapper;
import com.dragun.ecommerce.integration.pancake.model.PancakeOrderMapping;
import com.dragun.ecommerce.integration.pancake.model.PancakeSyncLog;
import com.dragun.ecommerce.integration.pancake.model.PancakeCatalogEntry;
import com.dragun.ecommerce.integration.pancake.repository.PancakeCatalogEntryRepository;
import com.dragun.ecommerce.integration.pancake.repository.PancakeOrderMappingRepository;
import com.dragun.ecommerce.integration.pancake.repository.PancakeSyncLogRepository;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.OrderItem;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.repository.OrderRepository;
import com.dragun.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PancakeOrderSyncService {
    
    private final PancakeApiClient pancakeApiClient;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final PancakeOrderMappingRepository mappingRepository;
    private final PancakeSyncLogRepository syncLogRepository;
    private final PancakeIntegrationConfig config;
    private final PancakeCatalogEntryRepository catalogEntryRepository;
    
    /**
     * Sync orders from Pancake to Thi Yen
     */
    @Transactional
    public int syncFromPancake() {
        return syncFromPancake(false).imported();
    }

    /**
     * @param forceResync when {@code true}, re-import orders even if {@code pancake_imported=true}
     */
    @Transactional
    public PancakeOrderSyncBatchResult syncFromPancake(boolean forceResync) {
        if (!config.getSync().getEnabled()
                || !PancakeIntegrationConstants.isSyncFromPancakeEnabled(config.getSync().getDirection())) {
            log.info("Sync from Pancake is disabled");
            return new PancakeOrderSyncBatchResult(0, 0, 0);
        }

        try {
            List<PancakeOrderDto> pancakeOrders = pancakeApiClient.getOrders().block();
            if (pancakeOrders == null || pancakeOrders.isEmpty()) {
                log.info("No orders found in Pancake");
                return new PancakeOrderSyncBatchResult(0, 0, 0);
            }

            int imported = 0;
            int skipped = 0;
            int failed = 0;
            for (PancakeOrderDto pancakeOrder : pancakeOrders) {
                if (pancakeOrder.getId() == null || pancakeOrder.getId().isBlank()) {
                    continue;
                }
                if (shouldSkipPancakeImport(pancakeOrder.getId(), forceResync)) {
                    skipped++;
                    log.debug("Skipping already imported Pancake order id={}", pancakeOrder.getId());
                    logSync(PancakeIntegrationConstants.SYNC_ENTITY_ORDER, pancakeOrder.getId(),
                            PancakeIntegrationConstants.SYNC_DIRECTION_FROM_PANCAKE,
                            PancakeIntegrationConstants.SYNC_LOG_STATUS_SKIPPED,
                            PancakeIntegrationConstants.SYNC_SKIP_REASON_ALREADY_IMPORTED);
                    continue;
                }
                try {
                    syncOrderFromPancake(pancakeOrder, forceResync);
                    imported++;
                    logSync(PancakeIntegrationConstants.SYNC_ENTITY_ORDER, pancakeOrder.getId(),
                            PancakeIntegrationConstants.SYNC_DIRECTION_FROM_PANCAKE,
                            PancakeIntegrationConstants.SYNC_LOG_STATUS_SUCCESS, null);
                } catch (Exception e) {
                    failed++;
                    log.error("Error syncing order {} from Pancake: {}", pancakeOrder.getId(), e.getMessage());
                    logSync(PancakeIntegrationConstants.SYNC_ENTITY_ORDER, pancakeOrder.getId(),
                            PancakeIntegrationConstants.SYNC_DIRECTION_FROM_PANCAKE,
                            PancakeIntegrationConstants.SYNC_LOG_STATUS_FAILED, e.getMessage());
                }
            }

            log.info("Pancake order import: imported={}, skipped={}, failed={}", imported, skipped, failed);
            return new PancakeOrderSyncBatchResult(imported, skipped, failed);
        } catch (Exception e) {
            log.error("Error syncing orders from Pancake: {}", e.getMessage());
            return new PancakeOrderSyncBatchResult(0, 0, 0);
        }
    }
    
    /**
     * Sync orders from Thi Yen to Pancake
     */
    @Transactional
    public int syncToPancake() {
        if (!config.getSync().getEnabled()
                || !PancakeIntegrationConstants.isSyncToPancakeEnabled(config.getSync().getDirection())) {
            log.info("Sync to Pancake is disabled");
            return 0;
        }
        
        try {
            // Get orders that need syncing (not synced or updated after last sync)
            List<Order> ordersToSync = orderRepository.findOrdersNeedingSync(
                    LocalDateTime.now().minusDays(1));
            
            if (ordersToSync.isEmpty()) {
                log.info("No orders to sync to Pancake");
                return 0;
            }
            
            int syncedCount = 0;
            for (Order order : ordersToSync) {
                try {
                    syncOrderToPancake(order);
                    syncedCount++;
                    logSync(PancakeIntegrationConstants.SYNC_ENTITY_ORDER, order.getOrderId(),
                            PancakeIntegrationConstants.SYNC_DIRECTION_TO_PANCAKE,
                            PancakeIntegrationConstants.SYNC_LOG_STATUS_SUCCESS, null);
                } catch (Exception e) {
                    log.error("Error syncing order {} to Pancake: {}", order.getOrderId(), e.getMessage());
                    logSync(PancakeIntegrationConstants.SYNC_ENTITY_ORDER, order.getOrderId(),
                            PancakeIntegrationConstants.SYNC_DIRECTION_TO_PANCAKE,
                            PancakeIntegrationConstants.SYNC_LOG_STATUS_FAILED, e.getMessage());
                }
            }
            
            log.info("Synced {} orders to Pancake", syncedCount);
            return syncedCount;
        } catch (Exception e) {
            log.error("Error syncing orders to Pancake: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Sync a single order from Pancake
     */
    @Transactional
    public Order syncOrderFromPancake(PancakeOrderDto pancakeOrder) {
        return syncOrderFromPancake(pancakeOrder, false);
    }

    @Transactional
    public Order syncOrderFromPancake(PancakeOrderDto pancakeOrder, boolean forceResync) {
        String pancakeOrderId = pancakeOrder.getId();
        if (pancakeOrderId != null && shouldSkipPancakeImport(pancakeOrderId, forceResync)) {
            return orderRepository.findByPancakeOrderIdWithItems(pancakeOrderId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Pancake order already imported: " + pancakeOrderId));
        }

        pancakeOrder = enrichOrderFromPancakeApi(pancakeOrder);
        Product unmappedLineProduct = requireUnmappedLineProduct();

        Optional<Order> existingOrderOpt = orderRepository.findByPancakeOrderIdWithItems(pancakeOrder.getId());

        Order order;
        if (existingOrderOpt.isPresent()) {
            order = existingOrderOpt.get();
            orderMapper.updateThiYenOrder(order, pancakeOrder);
            if (pancakeOrder.getItems() != null && !pancakeOrder.getItems().isEmpty()) {
                orderMapper.replaceOrderItemsFromPancake(
                        order,
                        pancakeOrder.getItems(),
                        this::resolveProductForLine,
                        unmappedLineProduct);
            }
        } else {
            Optional<PancakeOrderMapping> mappingOpt = mappingRepository.findByPancakeOrderId(pancakeOrder.getId());
            if (mappingOpt.isPresent()) {
                Long localOrderId = mappingOpt.get().getLocalOrder().getId();
                order = orderRepository.findByIdWithItems(localOrderId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Pancake mapping references missing order id=" + localOrderId));
                orderMapper.updateThiYenOrder(order, pancakeOrder);
                if (pancakeOrder.getItems() != null && !pancakeOrder.getItems().isEmpty()) {
                    orderMapper.replaceOrderItemsFromPancake(
                            order,
                            pancakeOrder.getItems(),
                            this::resolveProductForLine,
                            unmappedLineProduct);
                }
            } else {
                order = orderMapper.toThiYenOrder(pancakeOrder);

                if (pancakeOrder.getItems() != null && !pancakeOrder.getItems().isEmpty()) {
                    List<OrderItem> orderItems = orderMapper.createOrderItemsFromPancake(
                            order,
                            pancakeOrder.getItems(),
                            this::resolveProductForLine,
                            unmappedLineProduct);
                    order.setItems(orderItems);
                }
            }
        }
        
        order.setPancakeOrderId(pancakeOrder.getId());
        order.setPancakeImported(true);
        order.setPancakeSyncedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // Update or create mapping
        Optional<PancakeOrderMapping> mappingOpt = mappingRepository.findByLocalOrderId(order.getId());
        PancakeOrderMapping mapping;
        if (mappingOpt.isPresent()) {
            mapping = mappingOpt.get();
            mapping.setPancakeOrderId(pancakeOrder.getId());
        } else {
            mapping = PancakeOrderMapping.builder()
                    .localOrder(order)
                    .pancakeOrderId(pancakeOrder.getId())
                    .build();
        }
        mapping.setLastSyncedAt(LocalDateTime.now());
        mappingRepository.save(mapping);
        
        return order;
    }
    
    /**
     * Sync a single order to Pancake
     */
    @Transactional
    public PancakeOrderDto syncOrderToPancake(Order order) {
        PancakeOrderDto pancakeOrder = orderMapper.toPancakeOrder(order);
        
        if (order.getPancakeOrderId() != null) {
            // Update existing order in Pancake
            pancakeOrder = pancakeApiClient.updateOrder(order.getPancakeOrderId(), pancakeOrder).block();
        } else {
            // Create new order in Pancake
            pancakeOrder = pancakeApiClient.createOrder(pancakeOrder).block();
            if (pancakeOrder != null && pancakeOrder.getId() != null) {
                order.setPancakeOrderId(pancakeOrder.getId());
                order.setPancakeSyncedAt(LocalDateTime.now());
                orderRepository.save(order);
                
                // Create mapping
                PancakeOrderMapping mapping = PancakeOrderMapping.builder()
                        .localOrder(order)
                        .pancakeOrderId(pancakeOrder.getId())
                        .build();
                mappingRepository.save(mapping);
            }
        }
        
        if (pancakeOrder != null) {
            order.setPancakeSyncedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
        
        return pancakeOrder;
    }
    
    /**
     * List endpoint often returns summary rows without customer/items — load detail when needed.
     */
    private PancakeOrderDto enrichOrderFromPancakeApi(PancakeOrderDto summary) {
        if (summary == null || summary.getId() == null || summary.getId().isBlank()) {
            return summary;
        }
        boolean needsDetail = !orderMapper.hasCustomerPayload(summary)
                || summary.getItems() == null
                || summary.getItems().isEmpty();
        if (!needsDetail) {
            return summary;
        }
        try {
            PancakeOrderDto detail = pancakeApiClient.getOrderById(summary.getId()).block();
            if (detail != null) {
                return orderMapper.mergeOrderDetail(summary, detail);
            }
        } catch (Exception e) {
            log.warn("Could not load Pancake order detail for id={}: {}", summary.getId(), e.getMessage());
        }
        return summary;
    }

    /**
     * Match POS line to local catalog: variation SKU first (Pancake variant id), then base product id.
     */
    private Product resolveProductForLine(PancakeOrderDto.PancakeOrderItem line) {
        String variationId = line.getVariationId() != null ? line.getVariationId().trim() : "";
        String productId = line.getProductId() != null ? line.getProductId().trim() : "";

        if (!variationId.isBlank()) {
            Optional<Product> byVariation = productRepository.findByPancakeProductId(variationId);
            if (byVariation.isPresent()) {
                return byVariation.get();
            }
        }
        if (!productId.isBlank()) {
            Optional<Product> byProduct = productRepository.findByPancakeProductId(productId);
            if (byProduct.isPresent()) {
                return byProduct.get();
            }
        }

        String shopId = config.getApi().getShopId();
        if (shopId != null && !shopId.isBlank()) {
            shopId = shopId.trim();
            Optional<PancakeCatalogEntry> catalogHit = Optional.empty();
            if (!variationId.isBlank() && !productId.isBlank()) {
                catalogHit = catalogEntryRepository.findFirstByShopIdAndPancakeProductIdAndPancakeVariationId(
                        shopId, productId, variationId);
            }
            if (catalogHit.isEmpty() && !variationId.isBlank()) {
                catalogHit = catalogEntryRepository.findFirstByShopIdAndPancakeVariationId(shopId, variationId);
            }
            if (catalogHit.isPresent() && catalogHit.get().getLocalProduct() != null) {
                return catalogHit.get().getLocalProduct();
            }
        }
        return null;
    }

    private boolean shouldSkipPancakeImport(String pancakeOrderId, boolean forceResync) {
        if (forceResync) {
            return false;
        }
        if (!Boolean.TRUE.equals(config.getSync().getSkipAlreadyImportedOrders())) {
            return false;
        }
        return orderRepository.findByPancakeOrderId(pancakeOrderId)
                .map(order -> Boolean.TRUE.equals(order.getPancakeImported()))
                .orElse(false);
    }

    private Product requireUnmappedLineProduct() {
        return productRepository
                .findByPancakeProductId(PancakeCatalogConstants.UNMAPPED_LINE_PANCAKE_PRODUCT_ID)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing placeholder product pancake_product_id="
                                + PancakeCatalogConstants.UNMAPPED_LINE_PANCAKE_PRODUCT_ID
                                + "; apply Flyway migration V17"));
    }
    
    private void logSync(String entityType, String entityId, String direction, String status, String errorMessage) {
        PancakeSyncLog log = PancakeSyncLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .syncDirection(direction)
                .status(status)
                .errorMessage(errorMessage)
                .build();
        syncLogRepository.save(log);
    }
}

