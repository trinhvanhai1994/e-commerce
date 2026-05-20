package com.dragun.ecommerce.integration.pancake.scheduler;

import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.service.PancakeOrderSyncService;
import com.dragun.ecommerce.integration.pancake.service.PancakeProductSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PancakeSyncScheduler {
    
    private final PancakeProductSyncService productSyncService;
    private final PancakeOrderSyncService orderSyncService;
    private final PancakeIntegrationConfig config;
    
    @Scheduled(cron = "${pancake.sync.schedule.products.cron:0 */2 * * * *}")
    public void syncProducts() {
        if (!config.getSync().getEnabled() || !config.getSync().getSchedule().getProducts().getEnabled()) {
            log.debug("Product sync scheduled task is disabled");
            return;
        }
        
        log.info("Starting scheduled product sync (import from Pancake only; push disabled in scheduler)");
        try {
            int fromCount = productSyncService.syncFromPancake();
            log.info("Scheduled product sync completed: {} imported from Pancake", fromCount);
        } catch (Exception e) {
            log.error("Error in scheduled product sync: {}", e.getMessage(), e);
        }
    }
    
    @Scheduled(cron = "${pancake.sync.schedule.orders.cron:0 */2 * * * *}")
    public void syncOrders() {
        if (!config.getSync().getEnabled() || !config.getSync().getSchedule().getOrders().getEnabled()) {
            log.debug("Order sync scheduled task is disabled");
            return;
        }
        
        log.info("Starting scheduled order sync");
        try {
            int fromCount = orderSyncService.syncFromPancake();
            int toCount = orderSyncService.syncToPancake();
            log.info("Scheduled order sync completed: {} from Pancake, {} to Pancake", fromCount, toCount);
        } catch (Exception e) {
            log.error("Error in scheduled order sync: {}", e.getMessage(), e);
        }
    }
}

