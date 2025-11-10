package com.dragun.ecommerce.integration.config;

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
        private String baseUrl = "https://api.pancake.vn";
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
        private Boolean enabled = false;
        private String direction = "BIDIRECTIONAL";
        private Schedule schedule = new Schedule();
        
        @Data
        public static class Schedule {
            private Products products = new Products();
            private Orders orders = new Orders();
            
            @Data
            public static class Products {
                private Boolean enabled = false;
                private String cron = "0 */30 * * * *";
            }
            
            @Data
            public static class Orders {
                private Boolean enabled = false;
                private String cron = "0 */15 * * * *";
            }
        }
    }
}

