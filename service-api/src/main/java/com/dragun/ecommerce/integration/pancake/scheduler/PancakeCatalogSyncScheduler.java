package com.dragun.ecommerce.integration.pancake.scheduler;

import com.dragun.ecommerce.integration.config.PancakeIntegrationConfig;
import com.dragun.ecommerce.integration.pancake.service.PancakeCatalogFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically pulls Pancake POS product catalog into {@code pancake_catalog_entry} for mapping with Thiyen DB and MeInvoice.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PancakeCatalogSyncScheduler {

    private final PancakeCatalogFetchService catalogFetchService;
    private final PancakeIntegrationConfig config;

    @Scheduled(cron = "${pancake.sync.schedule.catalog.cron:0 0 */6 * * *}")
    public void fetchCatalog() {
        if (!config.getSync().getEnabled() || !config.getSync().getSchedule().getCatalog().getEnabled()) {
            log.debug("Pancake catalog fetch scheduler is disabled");
            return;
        }

        log.info("Starting scheduled Pancake catalog fetch");
        try {
            PancakeCatalogFetchService.CatalogFetchResult result = catalogFetchService.fetchAndLinkCatalog();
            log.info("Scheduled Pancake catalog fetch completed: upserted={}, linked={}, total={}",
                    result.entriesUpserted(), result.entriesLinked(), result.totalEntries());
        } catch (Exception e) {
            log.error("Scheduled Pancake catalog fetch failed: {}", e.getMessage(), e);
        }
    }
}
