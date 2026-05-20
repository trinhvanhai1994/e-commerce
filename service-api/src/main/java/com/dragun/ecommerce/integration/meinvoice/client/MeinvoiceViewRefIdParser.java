package com.dragun.ecommerce.integration.meinvoice.client;

import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

/**
 * Parses {@code GET /webapp/viewrefid} response: JSON {@code data} (base64) or raw PDF bytes.
 */
public final class MeinvoiceViewRefIdParser {

    private MeinvoiceViewRefIdParser() {
    }

    public static byte[] parseHttpBody(byte[] bodyBytes, ObjectMapper objectMapper) {
        if (bodyBytes == null || bodyBytes.length == 0) {
            throw new IllegalStateException(MeinvoiceIntegrationConstants.ERROR_VIEW_REF_ID_EMPTY_BODY);
        }
        if (MeinvoiceIntegrationConstants.isPdfBytes(bodyBytes)) {
            return bodyBytes;
        }
        String text = new String(bodyBytes, StandardCharsets.UTF_8).trim();
        if (text.startsWith(MeinvoiceIntegrationConstants.JSON_PREFIX_OBJECT)
                || text.startsWith(MeinvoiceIntegrationConstants.JSON_PREFIX_ARRAY)) {
            return decodePdfFromJsonText(objectMapper, text);
        }
        if (looksLikeBase64Payload(text)) {
            return decodeBase64Pdf(stripDataUrlPrefix(text));
        }
        throw new IllegalStateException(String.format(
                Locale.ROOT,
                MeinvoiceIntegrationConstants.ERROR_VIEW_REF_ID_UNRECOGNIZED_FORMAT,
                bodyBytes.length,
                abbreviate(text, 80)));
    }

    private static byte[] decodePdfFromJsonText(ObjectMapper objectMapper, String jsonText) {
        try {
            JsonNode root = objectMapper.readTree(jsonText);
            return MeinvoicePreviewPdfParser.parsePdfBytesFromApiJson(
                    root,
                    MeinvoiceIntegrationConstants.OPERATION_VIEW_REF_ID);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_VIEW_REF_ID_JSON_PARSE_FORMAT,
                    e.getMessage()), e);
        }
    }

    private static byte[] decodeBase64Pdf(String base64) {
        try {
            byte[] pdf = Base64.getDecoder().decode(base64);
            if (!MeinvoiceIntegrationConstants.isPdfBytes(pdf)) {
                throw new IllegalStateException(MeinvoiceIntegrationConstants.ERROR_PREVIEW_PDF_INVALID);
            }
            return pdf;
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(MeinvoiceIntegrationConstants.ERROR_PREVIEW_PDF_INVALID, e);
        }
    }

    private static boolean looksLikeBase64Payload(String text) {
        if (text.length() < 32) {
            return false;
        }
        int sampleLength = Math.min(text.length(), 256);
        return text.substring(0, sampleLength).matches("^[A-Za-z0-9+/=\\s\\r\\n-]+$");
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

    private static String abbreviate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        return text.length() <= maxLength ? text : String.format(Locale.ROOT, "%s...", text.substring(0, maxLength));
    }
}
