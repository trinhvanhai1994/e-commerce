package com.dragun.ecommerce.integration.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/** {@code app.mail.*} in application.yml. Secrets and addresses come from environment variables. */
@Data
@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {

    /** Off by default: without it (or without an API key or sender) mails are only logged. */
    private boolean enabled;

    /** Sender address; must be a sender or domain verified in Brevo. */
    private String fromEmail;

    private String fromName = "Thi Yên";

    /** Who receives the "new order" mail. Empty: that mail is skipped. */
    private List<String> adminRecipients = new ArrayList<>();

    /** Storefront base URL used for links in mails, e.g. https://thiyen.vn. Empty: mails carry no links. */
    private String storeUrl;

    /** Shown in the customer mail when set. */
    private String supportPhone;

    private Brevo brevo = new Brevo();

    public boolean canSend() {
        return enabled && StringUtils.hasText(fromEmail) && StringUtils.hasText(brevo.getApiKey());
    }

    public List<String> adminRecipientList() {
        return adminRecipients == null ? List.of()
            : adminRecipients.stream().filter(StringUtils::hasText).map(String::trim).toList();
    }

    @Data
    public static class Brevo {
        /** Key from Brevo "API keys" tab. An SMTP key (xsmtpsib-...) gets 401 on the REST API. */
        private String apiKey;
        private String baseUrl = "https://api.brevo.com";
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(30);
    }
}
