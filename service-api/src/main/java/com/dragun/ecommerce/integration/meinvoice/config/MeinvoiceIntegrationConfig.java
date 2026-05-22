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

    private Publish publish = new Publish();

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
        /** V2 {@code POST /auth/token} — {@code appid} field. */
        private String appId = "";
    }

    @Data
    public static class Publish {
        private boolean enabled = false;
        /** HSM / thường (ký tự thứ 5 của InvSeries = T). */
        private int signType = 2;
        /** Hóa đơn máy tính tiền (ký tự thứ 5 = M). MISA doc: SignType 5. */
        private int signTypeMtt = 5;
        private String certificateSn = "";
        private long sequentialDelayMs = 3000L;
        private boolean sendEmail = false;
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
        /**
         * {@code invoiceCalcu} for V2 status/download. {@code null} = auto from {@link #invSeries} (M at char 5).
         */
        private Boolean invoiceCalculatingMachine;
    }
}
