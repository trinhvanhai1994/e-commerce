package com.dragun.ecommerce.integration.meinvoice;

import java.util.Locale;

/**
 * MeInvoice (MISA) integration literals — API paths, JSON fields, validation codes, messages.
 */
public final class MeinvoiceIntegrationConstants {

    public static final String INV_NO_PLACEHOLDER = "<Chưa cấp số>";

    public static final String PAYMENT_METHOD_CASH_OR_TRANSFER = "TM/CK";
    public static final String PAYMENT_METHOD_CASH = "Tiền mặt";
    public static final String PAYMENT_METHOD_TRANSFER = "CK";

    public static final String SHIPPING_LINE_DESCRIPTION = "Phí vận chuyển";
    public static final String SHIPPING_LINE_UNIT_NAME = "Lần";

    public static final String LOOKUP_BY_ORDER = "order";
    public static final String LOOKUP_BY_PANCAKE = "pancake";

    public static boolean isLookupByPancake(String by) {
        return LOOKUP_BY_PANCAKE.equalsIgnoreCase(by);
    }

    public static final String API_PATH_TOKEN = "/webapp/token";
    public static final String API_PATH_TEMPLATES = "/webapp/templates";
    public static final String API_PATH_PREVIEW = "/webapp/preview";
    public static final String API_PATH_INSERT = "/webapp/insert";
    public static final String API_PATH_GET_LIST = "/webapp/getlist";
    public static final String API_PATH_VIEW_REF_ID = "/webapp/viewrefid";
    public static final String API_PATH_DELETE = "/webapp/delete";

    public static final String QUERY_PARAM_INVOICE_WITH_CODE = "invoiceWithCode";
    public static final String QUERY_PARAM_REF_ID = "refid";

    public static final String HEADER_TAXCODE = "taxcode";
    public static final String HEADER_AUTHORIZATION_BEARER_FORMAT = "Bearer %s";
    public static final String HEADER_ACCEPT_PDF_AND_JSON = "application/json,application/pdf,*/*";

    public static final String JSON_FIELD_SUCCESS = "success";
    public static final String JSON_FIELD_DATA = "data";
    public static final String JSON_FIELD_ERROR = "error";
    public static final String JSON_FIELD_ERROR_DESCRIPTION = "error_description";
    public static final String JSON_FIELD_ERROR_CODE = "errorCode";
    public static final String JSON_FIELD_ERRORS = "errors";

    public static final String JSON_VALUE_SUCCESS_TRUE = "true";

    public static final int PDF_MAGIC_BYTE_PERCENT = 0x25;
    public static final int PDF_MAGIC_BYTE_P = 0x50;
    public static final int PDF_MAGIC_BYTE_D = 0x44;
    public static final int PDF_MAGIC_BYTE_F = 0x46;
    public static final int PDF_MIN_HEADER_LENGTH = 4;

    public static final int GET_LIST_MAX_REF_IDS = 50;
    public static final int SUBMISSION_MESSAGE_MAX_LENGTH = 8000;
    public static final long MIN_REQUEST_TIMEOUT_MS = 1_000L;

    public static final String MIME_TYPE_APPLICATION_PDF = "application/pdf";
    public static final String CONTENT_DISPOSITION_ATTACHMENT_FILENAME_FORMAT = "attachment; filename=\"meinvoice-%s.pdf\"";
    public static final String REF_ID_FILENAME_SAFE_PATTERN = "[^a-zA-Z0-9-]";

    public static final String OPERATION_TOKEN = "token";
    public static final String OPERATION_TEMPLATES = "templates";
    public static final String OPERATION_GET_LIST = "getlist";
    public static final String OPERATION_PREVIEW = "preview";
    public static final String OPERATION_VIEW_REF_ID = "viewrefid";
    public static final String OPERATION_DELETE = "delete";

    public static final String RESPONSE_FIELD_PDF_BASE64 = "pdfBase64";
    /** MTT draft: RefID stored locally; V2 publish assigns InvNo / TransactionID / InvCode. */
    public static final String RESPONSE_FIELD_DRAFT_SOURCE = "draftSource";
    public static final String DRAFT_SOURCE_MTT_V2_PUBLISH = "mtt-v2-publish";

    public static final String ERROR_HTTP_GET_FORMAT = "MeInvoice GET %s HTTP %s: %s";
    public static final String ERROR_HTTP_POST_FORMAT = "MeInvoice POST %s HTTP %s: %s";
    public static final String ERROR_HTTP_DELETE_FORMAT = "MeInvoice DELETE %s HTTP %s: %s";
    public static final String ERROR_API_SUCCESS_FALSE_FORMAT = "MeInvoice %s returned success=false: %s";
    public static final String ERROR_PREVIEW_PDF_INVALID = "MeInvoice preview: decoded data is not a PDF (%PDF)";
    public static final String ERROR_VIEW_REF_ID_EMPTY_BODY = "MeInvoice viewrefid: empty response body";
    public static final String ERROR_VIEW_REF_ID_JSON_PARSE_FORMAT = "MeInvoice viewrefid: invalid JSON: %s";
    public static final String ERROR_VIEW_REF_ID_UNRECOGNIZED_FORMAT = "MeInvoice viewrefid: unrecognized body (length=%d, prefix=%s)";
    public static final String ERROR_OPERATION_FAILED_FORMAT = "MeInvoice %s failed: %s";
    public static final String ERROR_OPERATION_ERROR_TEXT_FORMAT = "MeInvoice %s error: %s";
    public static final String ERROR_OPERATION_EMPTY_DATA_FORMAT = "MeInvoice %s returned empty data";
    public static final String ERROR_OPERATION_UNSUPPORTED_DATA_FORMAT = "MeInvoice %s: unsupported data shape: %s";

    public static final String ERROR_INTEGRATION_DISABLED =
            "MeInvoice integration is disabled. Set meinvoice.enabled=true after configuring credentials.";
    public static final String ERROR_CREDENTIALS_INCOMPLETE =
            "MeInvoice credentials are incomplete. Set MEINVOICE_TAXCODE, MEINVOICE_USERNAME, MEINVOICE_PASSWORD.";
    public static final String ERROR_TEMPLATE_NOT_CONFIGURED =
            "MeInvoice template is not configured. Set meinvoice.defaults.invoice-template-id and inv-series.";
    public static final String ERROR_REF_ID_REQUIRED = "refId is required";
    public static final String ERROR_ORDER_KEY_REQUIRED = "orderKey is required";
    public static final String ERROR_REF_IDS_EMPTY = "refIds must contain between 1 and 50 RefID values.";
    public static final String ERROR_REF_IDS_TOO_MANY = "MeInvoice getlist allows at most 50 RefID per request.";
    public static final String ERROR_REF_IDS_BLANK_ENTRY = "refIds must not contain blank entries.";
    public static final String ERROR_ORDER_NOT_READY_PREVIEW_FORMAT = "Order not ready for MeInvoice preview: %s";
    public static final String ERROR_ORDER_NOT_READY_INSERT_FORMAT = "Order not ready for MeInvoice insert: %s";
    public static final String ERROR_PANCAKE_ORDER_NOT_FOUND_FORMAT = "Pancake order not found: %s";
    public static final String ERROR_ORDER_NOT_FOUND_FORMAT = "Order not found by orderId or pancakeOrderId: %s";
    public static final String ERROR_ORDER_ALREADY_INVOICED_FORMAT =
            "Order already invoiced on MeInvoice (meinvoice_invoiced=true, refId=%s, orderId=%s)";

    public static final String SUBMISSION_ERROR_CODE_EXCEPTION = "Exception";
    public static final String SUBMISSION_ERROR_CODE_DELETED = "DELETED";
    public static final String SUBMISSION_MESSAGE_DELETED = "Draft invoice deleted on MeInvoice";

    public static final String VALIDATION_MEINVOICE_DISABLED = "MEINVOICE_DISABLED";
    public static final String VALIDATION_MEINVOICE_TEMPLATE_NOT_CONFIGURED = "MEINVOICE_TEMPLATE_NOT_CONFIGURED";
    public static final String VALIDATION_NO_LINE_ITEMS = "NO_LINE_ITEMS";
    public static final String VALIDATION_MISSING_CUSTOMER_NAME = "MISSING_CUSTOMER_NAME";
    public static final String VALIDATION_MISSING_CUSTOMER_PHONE = "MISSING_CUSTOMER_PHONE";
    public static final String VALIDATION_MISSING_CUSTOMER_ADDRESS = "MISSING_CUSTOMER_ADDRESS";
    public static final String VALIDATION_MEINVOICE_ALREADY_INVOICED = "MEINVOICE_ALREADY_INVOICED";
    public static final String VALIDATION_MEINVOICE_ALREADY_PUBLISHED = "MEINVOICE_ALREADY_PUBLISHED";
    public static final String VALIDATION_MEINVOICE_DRAFT_DELETED = "MEINVOICE_DRAFT_DELETED";
    public static final String VALIDATION_MEINVOICE_NO_DRAFT_REF = "MEINVOICE_NO_DRAFT_REF";
    public static final String VALIDATION_UNMAPPED_CATALOG_LINE_PREFIX = "UNMAPPED_CATALOG_LINE:";
    public static final String VALIDATION_MISSING_ITEM_NAME = "MISSING_ITEM_NAME";
    public static final String VALIDATION_MISSING_ITEM_NAME_FORMAT = "%s:productId=%s";
    public static final String VALIDATION_ALREADY_INVOICED_REF_FORMAT = "%s:%s";

    public static final String DEFAULT_POS_CUSTOMER_NAME_FORMAT = "Khách POS #%s";
    public static final String DEFAULT_MISSING_PHONE_OR_ADDRESS = "-";
    public static final String UNKNOWN_PRODUCT_ID = "unknown";

    public static final String DATA_URL_PREFIX = "data:";
    public static final String JSON_PREFIX_OBJECT = "{";
    public static final String JSON_PREFIX_ARRAY = "[";

    public static final String[] JSON_DATA_PDF_FIELD_NAMES = {
            "FileData", "fileData", "PdfData", "pdfData", "Base64", "base64"
    };

    private MeinvoiceIntegrationConstants() {
    }

    public static String bearerToken(String accessToken) {
        return String.format(Locale.ROOT, HEADER_AUTHORIZATION_BEARER_FORMAT, accessToken);
    }

    public static boolean isPdfBytes(byte[] bytes) {
        return bytes != null
                && bytes.length >= PDF_MIN_HEADER_LENGTH
                && bytes[0] == PDF_MAGIC_BYTE_PERCENT
                && bytes[1] == PDF_MAGIC_BYTE_P
                && bytes[2] == PDF_MAGIC_BYTE_D
                && bytes[3] == PDF_MAGIC_BYTE_F;
    }
}
