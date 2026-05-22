package com.dragun.ecommerce.integration.meinvoice.client;

import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Builds actionable messages from MeInvoice HTTP/API error bodies.
 */
public final class MeinvoiceApiErrorParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MeinvoiceApiErrorParser() {
    }

    public static String formatHttpError(String operation, String path, HttpStatusCode status, String body) {
        return String.format(
                Locale.ROOT,
                MeinvoiceIntegrationConstants.ERROR_HTTP_POST_FORMAT,
                path,
                status.value(),
                summarizeBody(body));
    }

    public static String formatApiFailure(JsonNode root, String operation) {
        if (root == null) {
            return String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_OPERATION_FAILED_FORMAT,
                    operation,
                    "empty response");
        }
        return String.format(
                Locale.ROOT,
                MeinvoiceIntegrationConstants.ERROR_OPERATION_FAILED_FORMAT,
                operation,
                summarizeJson(root));
    }

    private static String summarizeBody(String body) {
        if (!StringUtils.hasText(body)) {
            return "empty body";
        }
        try {
            JsonNode json = MAPPER.readTree(body);
            return summarizeJson(json);
        } catch (Exception ignored) {
            return abbreviate(body.trim());
        }
    }

    private static String summarizeJson(JsonNode json) {
        boolean success = json.path(MeinvoiceIntegrationConstants.JSON_FIELD_SUCCESS).asBoolean(true);
        if (success) {
            return abbreviate(json.toString());
        }
        String error = textOrEmpty(json, MeinvoiceIntegrationConstants.JSON_FIELD_ERROR);
        String description = textOrEmpty(json, MeinvoiceIntegrationConstants.JSON_FIELD_ERROR_DESCRIPTION);
        if (StringUtils.hasText(error) && StringUtils.hasText(description)) {
            return error + " — " + description;
        }
        if (StringUtils.hasText(error)) {
            return error;
        }
        if (StringUtils.hasText(description)) {
            return description;
        }
        return abbreviate(json.toString());
    }

    private static String textOrEmpty(JsonNode json, String field) {
        String value = json.path(field).asText(null);
        return value == null ? "" : value.trim();
    }

    private static String abbreviate(String text) {
        int max = 500;
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max) + "...";
    }
}
