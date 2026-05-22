package com.dragun.ecommerce.integration.meinvoice.client;

import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

/**
 * Decodes PDF bytes from MeInvoice {@code POST /webapp/preview} JSON ({@code success} + {@code data} base64).
 */
public final class MeinvoicePreviewPdfParser {

    private MeinvoicePreviewPdfParser() {
    }

    public static byte[] parsePdfBytesFromApiJson(JsonNode root, String operation) {
        assertMeinvoiceSuccess(root, operation);
        assertNoErrorPayload(root, operation);

        JsonNode data = root.get(MeinvoiceIntegrationConstants.JSON_FIELD_DATA);
        if (data == null || data.isNull()) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_OPERATION_EMPTY_DATA_FORMAT,
                    operation));
        }
        if (data.isArray() && !data.isEmpty()) {
            for (JsonNode item : data) {
                byte[] fromItem = tryExtractBase64FromNode(item);
                if (fromItem != null) {
                    return fromItem;
                }
            }
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_OPERATION_UNSUPPORTED_DATA_FORMAT,
                    operation,
                    data.getNodeType()));
        }
        if (data.isTextual()) {
            String raw = data.asText().trim();
            if (raw.startsWith(MeinvoiceIntegrationConstants.JSON_PREFIX_OBJECT)
                    || raw.startsWith(MeinvoiceIntegrationConstants.JSON_PREFIX_ARRAY)) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode parsed = mapper.readTree(raw);
                    if (parsed.isArray() && !parsed.isEmpty()) {
                        for (JsonNode item : parsed) {
                            byte[] fromItem = tryExtractBase64FromNode(item);
                            if (fromItem != null) {
                                return fromItem;
                            }
                        }
                    }
                    return parsePdfBytesFromApiJson(parsed, operation);
                } catch (JsonProcessingException ignored) {
                    // nested JSON string failed — treat as base64 text
                }
            }
            return decodeBase64Pdf(stripDataUrlPrefix(raw));
        }
        if (data.isBinary()) {
            try {
                return requirePdfBytes(data.binaryValue());
            } catch (IOException e) {
                throw new IllegalStateException(String.format(
                        Locale.ROOT,
                        MeinvoiceIntegrationConstants.ERROR_OPERATION_UNSUPPORTED_DATA_FORMAT,
                        operation,
                        data.getNodeType()), e);
            }
        }
        for (String fieldName : MeinvoiceIntegrationConstants.JSON_DATA_PDF_FIELD_NAMES) {
            JsonNode nested = data.get(fieldName);
            if (nested != null && nested.isTextual() && StringUtils.hasText(nested.asText())) {
                return decodeBase64Pdf(stripDataUrlPrefix(nested.asText().trim()));
            }
        }
        throw new IllegalStateException(String.format(
                Locale.ROOT,
                MeinvoiceIntegrationConstants.ERROR_OPERATION_UNSUPPORTED_DATA_FORMAT,
                operation,
                data.getNodeType()));
    }

    private static byte[] tryExtractBase64FromNode(JsonNode item) {
        if (item == null || item.isNull()) {
            return null;
        }
        String base64 = item.path("Data").asText(null);
        if (!StringUtils.hasText(base64)) {
            base64 = item.path("data").asText(null);
        }
        if (!StringUtils.hasText(base64)) {
            for (String fieldName : MeinvoiceIntegrationConstants.JSON_DATA_PDF_FIELD_NAMES) {
                JsonNode nested = item.get(fieldName);
                if (nested != null && nested.isTextual() && StringUtils.hasText(nested.asText())) {
                    base64 = nested.asText().trim();
                    break;
                }
            }
        }
        if (!StringUtils.hasText(base64)) {
            return null;
        }
        try {
            return decodeBase64Pdf(stripDataUrlPrefix(base64.trim()));
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static byte[] decodeBase64Pdf(String base64) {
        try {
            return requirePdfBytes(Base64.getDecoder().decode(base64));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(MeinvoiceIntegrationConstants.ERROR_PREVIEW_PDF_INVALID, e);
        }
    }

    private static byte[] requirePdfBytes(byte[] pdf) {
        if (!MeinvoiceIntegrationConstants.isPdfBytes(pdf)) {
            throw new IllegalStateException(MeinvoiceIntegrationConstants.ERROR_PREVIEW_PDF_INVALID);
        }
        return pdf;
    }

    private static void assertMeinvoiceSuccess(JsonNode root, String operation) {
        if (!isSuccessFlag(root.path(MeinvoiceIntegrationConstants.JSON_FIELD_SUCCESS))) {
            String detail = root.path(MeinvoiceIntegrationConstants.JSON_FIELD_ERROR)
                    .asText(root.path(MeinvoiceIntegrationConstants.JSON_FIELD_ERROR_DESCRIPTION).asText(""));
            if (!StringUtils.hasText(detail) && root.has(MeinvoiceIntegrationConstants.JSON_FIELD_ERROR_CODE)) {
                detail = root.get(MeinvoiceIntegrationConstants.JSON_FIELD_ERROR_CODE).toString();
            }
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_OPERATION_FAILED_FORMAT,
                    operation,
                    StringUtils.hasText(detail) ? detail : root.toString()));
        }
    }

    private static void assertNoErrorPayload(JsonNode root, String operation) {
        JsonNode errNode = root.get(MeinvoiceIntegrationConstants.JSON_FIELD_ERROR);
        if (errNode != null && errNode.isTextual() && StringUtils.hasText(errNode.asText())) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_OPERATION_ERROR_TEXT_FORMAT,
                    operation,
                    errNode.asText()));
        }
        if (errNode != null && errNode.isArray() && !errNode.isEmpty()) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_OPERATION_ERROR_TEXT_FORMAT,
                    operation,
                    errNode));
        }
    }

    private static boolean isSuccessFlag(JsonNode successNode) {
        if (successNode == null || successNode.isNull() || successNode.isMissingNode()) {
            return false;
        }
        if (successNode.isBoolean()) {
            return successNode.asBoolean();
        }
        if (successNode.isTextual()) {
            return MeinvoiceIntegrationConstants.JSON_VALUE_SUCCESS_TRUE
                    .equalsIgnoreCase(successNode.asText().trim());
        }
        return false;
    }

    private static String stripDataUrlPrefix(String raw) {
        if (!StringUtils.hasText(raw)) {
            return raw;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith(MeinvoiceIntegrationConstants.DATA_URL_PREFIX)) {
            int comma = trimmed.indexOf(',');
            if (comma > 0) {
                return trimmed.substring(comma + 1).trim();
            }
        }
        return trimmed.replaceAll("\\s+", "");
    }
}
