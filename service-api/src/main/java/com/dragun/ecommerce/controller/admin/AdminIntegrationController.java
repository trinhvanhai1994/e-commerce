package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.client.PancakeApiClient;
import com.dragun.ecommerce.integration.pancake.model.PancakeSyncLog;
import com.dragun.ecommerce.integration.pancake.repository.PancakeSyncLogRepository;
import com.dragun.ecommerce.integration.pancake.service.PancakeOrderSyncService;
import com.dragun.ecommerce.integration.pancake.service.PancakeProductSyncService;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/thiyen/admin/integration/pancake")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminIntegrationController {
    
    private final PancakeProductSyncService productSyncService;
    private final PancakeOrderSyncService orderSyncService;
    private final PancakeApiClient pancakeApiClient;
    private final PancakeIntegrationConfig config;
    private final PancakeSyncLogRepository syncLogRepository;
    
    @PostMapping("/sync/products")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncProducts(
            @RequestParam(required = false, defaultValue = "BIDIRECTIONAL") String direction) {
        try {
            int fromCount = 0;
            int toCount = 0;
            
            if ("FROM_PANCAKE".equals(direction) || "BIDIRECTIONAL".equals(direction)) {
                fromCount = productSyncService.syncFromPancake();
            }
            
            if ("TO_PANCAKE".equals(direction) || "BIDIRECTIONAL".equals(direction)) {
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
            @RequestParam(required = false, defaultValue = "BIDIRECTIONAL") String direction) {
        try {
            int fromCount = 0;
            int toCount = 0;
            
            if ("FROM_PANCAKE".equals(direction) || "BIDIRECTIONAL".equals(direction)) {
                fromCount = orderSyncService.syncFromPancake();
            }
            
            if ("TO_PANCAKE".equals(direction) || "BIDIRECTIONAL".equals(direction)) {
                toCount = orderSyncService.syncToPancake();
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("syncedFromPancake", fromCount);
            result.put("syncedToPancake", toCount);
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
                "products", Map.of(
                        "enabled", config.getSync().getSchedule().getProducts().getEnabled(),
                        "cron", config.getSync().getSchedule().getProducts().getCron()
                ),
                "orders", Map.of(
                        "enabled", config.getSync().getSchedule().getOrders().getEnabled(),
                        "cron", config.getSync().getSchedule().getOrders().getCron()
                )
        ));
        
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

