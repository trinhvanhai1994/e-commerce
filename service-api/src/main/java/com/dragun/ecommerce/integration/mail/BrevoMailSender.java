package com.dragun.ecommerce.integration.mail;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Brevo transactional email REST API: POST /v3/smtp/email.
 * No retry on purpose: a POST whose response was lost may already be sent, and a retry would mail twice.
 */
@Slf4j
public class BrevoMailSender implements MailSender {

    static final String SEND_PATH = "/v3/smtp/email";

    private final RestClient restClient;
    private final MailProperties properties;

    /** @param builder carries the request factory (timeouts); this class adds the base URL and API key. */
    public BrevoMailSender(RestClient.Builder builder, MailProperties properties) {
        this.properties = properties;
        this.restClient = builder
            .baseUrl(properties.getBrevo().getBaseUrl())
            .defaultHeader("api-key", properties.getBrevo().getApiKey())
            .build();
    }

    @Override
    public boolean send(MailMessage message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sender", Map.of("name", properties.getFromName(), "email", properties.getFromEmail()));
        body.put("to", message.to().stream().map(email -> Map.of("email", email)).toList());
        body.put("subject", message.subject());
        body.put("htmlContent", message.html());
        body.put("textContent", message.text());
        try {
            JsonNode response = restClient.post()
                .uri(SEND_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
            log.info("Mail sent: subject='{}' to={} messageId={}", message.subject(), message.maskedRecipients(),
                response != null ? response.path("messageId").asText(null) : null);
            return true;
        } catch (RestClientResponseException e) {
            log.warn("Brevo rejected mail '{}' to={}: HTTP {} {}", message.subject(), message.maskedRecipients(),
                e.getStatusCode().value(), abbreviate(e.getResponseBodyAsString()));
            return false;
        } catch (RestClientException e) {
            log.warn("Brevo unreachable for mail '{}' to={}: {}", message.subject(), message.maskedRecipients(),
                e.getMessage());
            return false;
        }
    }

    private static String abbreviate(String text) {
        return text == null || text.length() <= 300 ? text : text.substring(0, 300) + "...";
    }
}
