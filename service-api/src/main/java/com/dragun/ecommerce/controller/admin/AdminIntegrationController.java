package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.PancakeIntegrationConstants;
import com.dragun.ecommerce.integration.pancake.client.PancakeApiClient;
import com.dragun.ecommerce.integration.pancake.model.PancakeSyncLog;
import com.dragun.ecommerce.integration.pancake.repository.PancakeSyncLogRepository;
import com.dragun.ecommerce.integration.pancake.dto.PancakeOrderSyncBatchResult;
import com.dragun.ecommerce.integration.pancake.service.PancakeCatalogFetchService;
import com.dragun.ecommerce.integration.pancake.service.PancakeOrderSyncService;
import com.dragun.ecommerce.integration.pancake.service.PancakeProductSyncService;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/thiyen/admin/integration/pancake")
@RequiredArgsConstructor
public class AdminIntegrationController {
    
    private final PancakeProductSyncService productSyncService;
    private final PancakeCatalogFetchService catalogFetchService;
    private final PancakeOrderSyncService orderSyncService;
    private final PancakeApiClient pancakeApiClient;
    private final PancakeIntegrationConfig config;
    private final PancakeSyncLogRepository syncLogRepository;
    
    @PostMapping("/catalog/fetch")
    public ResponseEntity<ApiResponse<Map<String, Object>>> fetchCatalog() {
        try {
            PancakeCatalogFetchService.CatalogFetchResult result = catalogFetchService.fetchAndLinkCatalog();
            Map<String, Object> data = new HashMap<>();
            data.put("entriesUpserted", result.entriesUpserted());
            data.put("entriesLinked", result.entriesLinked());
            data.put("totalEntries", result.totalEntries());
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(data)
                    .message("Pancake catalog fetched successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Error fetching Pancake catalog: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/sync/products")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncProducts(
            @RequestParam(required = false, defaultValue = PancakeIntegrationConstants.SYNC_DIRECTION_FROM_PANCAKE) String direction) {
        try {
            int fromCount = 0;
            int toCount = 0;

            if (PancakeIntegrationConstants.includesFromPancake(direction)) {
                catalogFetchService.fetchAndLinkCatalog();
                fromCount = productSyncService.syncFromPancake();
            }
            
            if (PancakeIntegrationConstants.includesToPancake(direction)) {
                toCount = productSyncService.syncToPancake();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("syncedFromPancake", fromCount);
            result.put("syncedToPancake", toCount);
            result.put("total", fromCount + toCount);
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(result)
                    .message("Product sync completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Error syncing products: " + e.getMessage())
                    .build());
        }
    }
    
    @PostMapping("/sync/orders")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncOrders(
            @RequestParam(required = false, defaultValue = PancakeIntegrationConstants.SYNC_DIRECTION_FROM_PANCAKE) String direction,
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        try {
            int fromCount = 0;
            int skippedFromPancake = 0;
            int failedFromPancake = 0;
            int toCount = 0;

            if (PancakeIntegrationConstants.includesFromPancake(direction)) {
                PancakeOrderSyncBatchResult batch = orderSyncService.syncFromPancake(force);
                fromCount = batch.imported();
                skippedFromPancake = batch.skipped();
                failedFromPancake = batch.failed();
            }

            if (PancakeIntegrationConstants.includesToPancake(direction)) {
                toCount = orderSyncService.syncToPancake();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("syncedFromPancake", fromCount);
            result.put("skippedAlreadyImported", skippedFromPancake);
            result.put("failedFromPancake", failedFromPancake);
            result.put("syncedToPancake", toCount);
            result.put("forceResync", force);
            result.put("total", fromCount + toCount);
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(result)
                    .message("Order sync completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Error syncing orders: " + e.getMessage())
                    .build());
        }
    }
    
    @GetMapping("/sync/logs")
    public ResponseEntity<ApiResponse<Page<PancakeSyncLog>>> getSyncLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PancakeSyncLog> logs;
        
        if (entityType != null && !entityType.isEmpty()) {
            logs = syncLogRepository.findByEntityTypeOrderBySyncedAtDesc(entityType, pageable);
        } else {
            logs = syncLogRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.<Page<PancakeSyncLog>>builder()
                .success(true)
                .data(logs)
                .message("Sync logs retrieved successfully")
                .build());
    }
    
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("enabled", config.getSync().getEnabled());
        configMap.put("direction", config.getSync().getDirection());
        configMap.put("apiBaseUrl", config.getApi().getBaseUrl());
        configMap.put("shopId", config.getApi().getShopId() != null ? "***" : null);
        configMap.put("hasApiKey", config.getApi().getApiKey() != null && !config.getApi().getApiKey().isEmpty());
        configMap.put("warehouseId", config.getApi().getWarehouseId());
        configMap.put("schedule", Map.of(
                "catalog", Map.of(
                        "enabled", config.getSync().getSchedule().getCatalog().getEnabled(),
                        "cron", config.getSync().getSchedule().getCatalog().getCron()
                ),
                "products", Map.of(
                        "enabled", config.getSync().getSchedule().getProducts().getEnabled(),
                        "cron", config.getSync().getSchedule().getProducts().getCron()
                ),
                "orders", Map.of(
                        "enabled", config.getSync().getSchedule().getOrders().getEnabled(),
                        "cron", config.getSync().getSchedule().getOrders().getCron()
                )
        ));
        configMap.put("apiBaseUrlHint", "Use " + PancakeIntegrationConstants.DEFAULT_API_BASE_URL + " (not api.pancake.vn)");
        
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(configMap)
                .message("Configuration retrieved successfully")
                .build());
    }
    
    @PostMapping("/test-connection")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testConnection() {
        try {
            Boolean connected = pancakeApiClient.testConnection().block();
            Map<String, Object> result = new HashMap<>();
            result.put("connected", connected);
            result.put("message", connected ? "Connection successful" : "Connection failed");
            
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(connected)
                    .data(result)
                    .message(connected ? "Connection test successful" : "Connection test failed")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Error testing connection: " + e.getMessage())
                    .build());
        }
    }
}

