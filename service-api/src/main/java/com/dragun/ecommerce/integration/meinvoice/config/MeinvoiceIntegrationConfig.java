package com.dragun.ecommerce.integration.meinvoice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "meinvoice")
@Data
public class MeinvoiceIntegrationConfig {

    private boolean enabled = false;

    private Api api = new Api();

    private Credentials credentials = new Credentials();

    private Defaults defaults = new Defaults();

    @Data
    public static class Api {
        private String baseUrl = "https://testapi.meinvoice.vn/api/integration";
        private int timeoutMs = 60000;
    }

    @Data
    public static class Credentials {
        private String taxcode = "";
        private String username = "";
        private String password = "";
    }

    @Data
    public static class Defaults {
        /**
         * Query param invoiceWithCode for MeInvoice APIs (có mã / không mã).
         */
        private boolean invoiceWithCode = true;
        /**
         * IPTemplateID from POST /webapp/templates response.
         */
        private String invoiceTemplateId = "";
        /**
         * InvSeries from template list.
         */
        private String invSeries = "";
        private String currencyCode = "VND";
        private int defaultVatRate = 10;
        private String defaultUnitName = "Cái";
        /**
         * When true, OrderItem.price is treated as unit price excluding VAT.
         */
        private boolean assumePricesExcludeVat = true;
    }
}
