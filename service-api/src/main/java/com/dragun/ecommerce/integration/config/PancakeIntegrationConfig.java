package com.dragun.ecommerce.integration.config;

import com.dragun.ecommerce.integration.pancake.PancakeIntegrationConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pancake")
@Data
public class PancakeIntegrationConfig {
    
    private Api api = new Api();
    private Sync sync = new Sync();
    
    @Data
    public static class Api {
        private String baseUrl = PancakeIntegrationConstants.DEFAULT_API_BASE_URL;
        private String shopId;
        private String apiKey;
        private String warehouseId;
        private Integer timeout = 30000;
        private Retry retry = new Retry();
        
        @Data
        public static class Retry {
            private Integer maxAttempts = 3;
            private Long backoffDelay = 1000L;
        }
    }
    
    @Data
    public static class Sync {
        private Boolean enabled = true;
        private String direction = PancakeIntegrationConstants.SYNC_DIRECTION_BIDIRECTIONAL;
        /**
         * When true, {@code syncFromPancake} skips orders with {@code pancake_imported=true}.
         */
        private Boolean skipAlreadyImportedOrders = true;
        private Schedule schedule = new Schedule();
        
        @Data
        public static class Schedule {
            private Catalog catalog = new Catalog();
            private Products products = new Products();
            private Orders orders = new Orders();

            @Data
            public static class Catalog {
                private Boolean enabled = true;
                /** Every 6 hours by default */
                private String cron = "0 0 */6 * * *";
            }
            
            @Data
            public static class Products {
                private Boolean enabled = true;
                private String cron = "0 */2 * * * *";
            }
            
            @Data
            public static class Orders {
                private Boolean enabled = true;
                private String cron = "0 */2 * * * *";
            }
        }
    }
}

