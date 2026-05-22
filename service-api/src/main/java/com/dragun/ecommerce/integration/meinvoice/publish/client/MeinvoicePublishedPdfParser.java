package com.dragun.ecommerce.integration.meinvoice.publish.client;

import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.dragun.ecommerce.integration.meinvoice.publish.MeinvoicePublishConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

/**
 * Parses {@code POST /invoice/download} PDF base64 from {@code data} (array, object, or JSON string).
 */
public final class MeinvoicePublishedPdfParser {

    private static final String JSON_FIELD_DATA_PAYLOAD = "Data";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private MeinvoicePublishedPdfParser() {
    }

    public static byte[] parsePdfBytes(JsonNode root) {
        if (root == null) {
            throw new IllegalStateException("MeInvoice download: empty response");
        }
        JsonNode data = root.get("data");
        if (data == null) {
            data = root.get("Data");
        }
        if (data == null || data.isNull()) {
            throw new IllegalStateException("MeInvoice download: missing data");
        }
        return parseDataNode(data);
    }

    private static byte[] parseDataNode(JsonNode data) {
        if (data.isArray()) {
            return extractFromArray(data);
        }
        if (data.isObject()) {
            if (data.has("PageData") && data.get("PageData").isArray()) {
                return extractFromArray(data.get("PageData"));
            }
            return extractFromInvoiceItem(data);
        }
        if (data.isTextual()) {
            String raw = data.asText().trim();
            if (!StringUtils.hasText(raw) || "[]".equals(raw)) {
                throw new IllegalStateException(MeinvoicePublishConstants.ERROR_DOWNLOAD_EMPTY_DATA);
            }
            if (raw.startsWith(MeinvoiceIntegrationConstants.JSON_PREFIX_OBJECT)
                    || raw.startsWith(MeinvoiceIntegrationConstants.JSON_PREFIX_ARRAY)) {
                try {
                    return parseDataNode(OBJECT_MAPPER.readTree(raw));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException(String.format(
                            Locale.ROOT,
                            "MeInvoice download: invalid JSON in data (%s)",
                            e.getMessage()), e);
                }
            }
            return decodePdf(stripDataUrlPrefix(raw));
        }
        throw new IllegalStateException(String.format(
                Locale.ROOT,
                "MeInvoice download: unsupported data shape (%s)",
                data.getNodeType()));
    }

    private static byte[] extractFromArray(JsonNode array) {
        if (array.isEmpty()) {
            throw new IllegalStateException(MeinvoicePublishConstants.ERROR_DOWNLOAD_EMPTY_DATA);
        }
        List<String> itemErrors = new ArrayList<>();
        for (JsonNode item : array) {
            if (item == null || item.isNull()) {
                continue;
            }
            if (item.isTextual() && StringUtils.hasText(item.asText())) {
                try {
                    return decodePdf(stripDataUrlPrefix(item.asText().trim()));
                } catch (IllegalStateException e) {
                    itemErrors.add(e.getMessage());
                    continue;
                }
            }
            String itemError = readItemError(item);
            if (StringUtils.hasText(itemError)) {
                itemErrors.add(itemError);
                continue;
            }
            try {
                return extractFromInvoiceItem(item);
            } catch (IllegalStateException e) {
                itemErrors.add(e.getMessage());
            }
        }
        if (itemErrors.isEmpty()) {
            throw new IllegalStateException(MeinvoicePublishConstants.ERROR_DOWNLOAD_NO_PDF_IN_ARRAY);
        }
        throw new IllegalStateException(String.join("; ", itemErrors));
    }

    private static String readItemError(JsonNode item) {
        String errorCode = textOrNull(item, MeinvoicePublishConstants.JSON_FIELD_ERROR_CODE);
        if (!StringUtils.hasText(errorCode)) {
            errorCode = textOrNull(item, "ErrorCode");
        }
        if (!StringUtils.hasText(errorCode)) {
            return null;
        }
        String transactionId = textOrNull(item, MeinvoicePublishConstants.JSON_FIELD_TRANSACTION_ID);
        if (!StringUtils.hasText(transactionId)) {
            transactionId = textOrNull(item, "TransactionID");
        }
        String description = textOrNull(item, "DescriptionErrorCode");
        if (!StringUtils.hasText(description)) {
            description = textOrNull(item, "descriptionErrorCode");
        }
        String label = StringUtils.hasText(transactionId) ? transactionId : "?";
        if (StringUtils.hasText(description)) {
            return String.format(
                    Locale.ROOT,
                    MeinvoicePublishConstants.ERROR_DOWNLOAD_ITEM_FORMAT,
                    label,
                    errorCode + " — " + description);
        }
        return String.format(
                Locale.ROOT,
                MeinvoicePublishConstants.ERROR_DOWNLOAD_ITEM_FORMAT,
                label,
                describeDownloadErrorCode(errorCode));
    }

    private static String describeDownloadErrorCode(String errorCode) {
        if (MeinvoicePublishConstants.ERROR_CODE_INVOICE_NOT_EXIST.equals(errorCode)) {
            return errorCode + " (hóa đơn không tồn tại trên MeInvoice — mã tra cứu có thể đã hết hạn hoặc sandbox đã xóa dữ liệu)";
        }
        if ("InvalidTransactionID".equals(errorCode)) {
            return errorCode + " (mã tra cứu không hợp lệ)";
        }
        return errorCode;
    }

    private static byte[] extractFromInvoiceItem(JsonNode item) {
        String base64 = textOrNull(item, JSON_FIELD_DATA_PAYLOAD);
        if (!StringUtils.hasText(base64)) {
            base64 = textOrNull(item, "data");
        }
        if (!StringUtils.hasText(base64)) {
            base64 = textOrNull(item, MeinvoicePublishConstants.JSON_FIELD_INVOICE_DATA);
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
            String transactionId = textOrNull(item, MeinvoicePublishConstants.JSON_FIELD_TRANSACTION_ID);
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    "MeInvoice download: missing PDF Data for %s",
                    StringUtils.hasText(transactionId) ? transactionId : "invoice"));
        }
        String trimmed = base64.trim();
        if (trimmed.startsWith(MeinvoiceIntegrationConstants.JSON_PREFIX_OBJECT)
                || trimmed.startsWith(MeinvoiceIntegrationConstants.JSON_PREFIX_ARRAY)) {
            try {
                return parseDataNode(OBJECT_MAPPER.readTree(trimmed));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(MeinvoiceIntegrationConstants.ERROR_PREVIEW_PDF_INVALID, e);
            }
        }
        return decodePdf(stripDataUrlPrefix(trimmed));
    }

    private static byte[] decodePdf(String base64) {
        if (!StringUtils.hasText(base64)) {
            throw new IllegalStateException("MeInvoice download: empty PDF data");
        }
        String cleaned = base64.replaceAll("\\s", "");
        try {
            byte[] decoded = Base64.getDecoder().decode(cleaned);
            if (MeinvoiceIntegrationConstants.isPdfBytes(decoded)) {
                return decoded;
            }
            if (looksLikeXml(decoded)) {
                throw new IllegalStateException(
                        "MeInvoice download: response is XML, not PDF — use downloadDataType=pdf");
            }
            throw new IllegalStateException(MeinvoiceIntegrationConstants.ERROR_PREVIEW_PDF_INVALID);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(MeinvoiceIntegrationConstants.ERROR_PREVIEW_PDF_INVALID, e);
        }
    }

    private static boolean looksLikeXml(byte[] bytes) {
        if (bytes == null || bytes.length < 5) {
            return false;
        }
        String head = new String(bytes, 0, Math.min(bytes.length, 32)).trim();
        return head.startsWith("<?xml") || head.startsWith("<");
    }

    private static String textOrNull(JsonNode node, String field) {
        if (node == null || !node.has(field)) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText(null);
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
