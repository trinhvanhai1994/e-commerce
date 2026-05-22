package com.dragun.ecommerce.integration.meinvoice.publish.client;

import com.dragun.ecommerce.integration.meinvoice.publish.MeinvoicePublishConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Parses {@code publishInvoiceResult} from V2 {@code POST /invoice}.
 */
public final class MeinvoicePublishResultParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MeinvoicePublishResultParser() {
    }

    public static PublishItemResult parseFirstItem(JsonNode root) {
        if (root == null) {
            throw new IllegalStateException(MeinvoicePublishConstants.ERROR_PUBLISH_RESULT_EMPTY);
        }
        JsonNode results = resolvePublishResultsArray(root);
        if (results == null || !results.isArray() || results.isEmpty()) {
            throw explainEmptyPublishResult(root);
        }
        return parseItem(results.get(0));
    }

    public static PublishItemResult parseItem(JsonNode item) {
        String errorCode = textOrNull(item, MeinvoicePublishConstants.JSON_FIELD_ERROR_CODE);
        if (!StringUtils.hasText(errorCode)) {
            errorCode = textOrNull(item, "ErrorCode");
        }
        if (StringUtils.hasText(errorCode)) {
            String description = textOrNull(item, "DescriptionErrorCode");
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoicePublishConstants.ERROR_PUBLISH_ITEM_FAILED_FORMAT,
                    textOrNull(item, "RefID"),
                    StringUtils.hasText(description) ? errorCode + " — " + description : errorCode));
        }
        String transactionId = textOrNull(item, MeinvoicePublishConstants.JSON_FIELD_TRANSACTION_ID);
        if (!StringUtils.hasText(transactionId)) {
            transactionId = textOrNull(item, "TransactionID");
        }
        String invNo = textOrNull(item, MeinvoicePublishConstants.JSON_FIELD_INV_NO);
        if (!StringUtils.hasText(invNo)) {
            invNo = textOrNull(item, "InvNo");
        }
        String invCode = textOrNull(item, "InvCode");
        if (!StringUtils.hasText(transactionId) && !StringUtils.hasText(invNo)) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoicePublishConstants.ERROR_PUBLISH_MISSING_IDENTIFIERS_FORMAT,
                    textOrNull(item, "RefID")));
        }
        return new PublishItemResult(transactionId, invNo, invCode, textOrNull(item, "RefID"));
    }

    private static JsonNode resolvePublishResultsArray(JsonNode root) {
        JsonNode results = root.path(MeinvoicePublishConstants.JSON_FIELD_PUBLISH_INVOICE_RESULT);
        if (results.isMissingNode() || results.isNull()) {
            results = root.path("PublishInvoiceResult");
        }
        if (results.isTextual()) {
            try {
                return MAPPER.readTree(results.asText());
            } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                        MeinvoicePublishConstants.ERROR_PUBLISH_RESULT_PARSE_FORMAT,
                        e);
            }
        }
        return results;
    }

    private static IllegalStateException explainEmptyPublishResult(JsonNode root) {
        JsonNode createResults = root.path(MeinvoicePublishConstants.JSON_FIELD_CREATE_INVOICE_RESULT);
        if (createResults.isMissingNode() || createResults.isNull()) {
            createResults = root.path("CreateInvoiceResult");
        }
        if (createResults.isArray() && !createResults.isEmpty()) {
            return new IllegalStateException(MeinvoicePublishConstants.ERROR_PUBLISH_GOT_CREATE_RESULT);
        }
        if (createResults.isTextual() && StringUtils.hasText(createResults.asText())) {
            return new IllegalStateException(MeinvoicePublishConstants.ERROR_PUBLISH_GOT_CREATE_RESULT);
        }
        return new IllegalStateException(MeinvoicePublishConstants.ERROR_PUBLISH_RESULT_EMPTY);
    }

    private static String textOrNull(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText(null);
    }

    public record PublishItemResult(String transactionId, String invNo, String invCode, String refId) {
    }
}
