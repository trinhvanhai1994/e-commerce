package com.dragun.ecommerce.integration.meinvoice.publish;

/**
 * MeInvoice V2 publish API literals ({@code /auth/token}, {@code /invoice/*}).
 */
public final class MeinvoicePublishConstants {

    public static final String API_PATH_AUTH_TOKEN = "/auth/token";
    public static final String API_PATH_INVOICE = "/invoice";
    public static final String API_PATH_GET_CERTIFICATES = "/invoice/get-certificates";
    public static final String API_PATH_INVOICE_STATUS = "/invoice/status";
    public static final String API_PATH_INVOICE_DOWNLOAD = "/invoice/download";

    public static final String JSON_FIELD_APP_ID = "appid";
    public static final String JSON_FIELD_SIGN_TYPE = "SignType";
    public static final String JSON_FIELD_CERTIFICATE_SN = "CertificateSN";
    public static final String JSON_FIELD_INVOICE_DATA = "InvoiceData";
    public static final String JSON_FIELD_PUBLISH_INVOICE_DATA = "PublishInvoiceData";
    public static final String JSON_FIELD_ORIGINAL_INVOICE_DETAIL = "OriginalInvoiceDetail";
    public static final String JSON_FIELD_TAX_RATE_INFO = "TaxRateInfo";

    public static final int ITEM_TYPE_NORMAL_GOODS = 1;
    public static final int ITEM_TYPE_PROMOTION = 2;

    public static final String VAT_RATE_NAME_FORMAT_PERCENT = "%d%%";
    public static final String VAT_RATE_NAME_FORMAT_OTHER = "KHAC:%s%%";

    public static final String JSON_FIELD_PUBLISH_INVOICE_RESULT = "publishInvoiceResult";
    public static final String JSON_FIELD_CREATE_INVOICE_RESULT = "createInvoiceResult";

    public static final String QUERY_PARAM_INVOICE_WITH_CODE = "invoiceWithCode";
    public static final String QUERY_PARAM_INPUT_TYPE = "inputType";
    public static final String QUERY_PARAM_DOWNLOAD_DATA_TYPE = "downloadDataType";
    public static final String QUERY_PARAM_INVOICE_CALCU = "invoiceCalcu";

    public static final String INPUT_TYPE_REF_ID = "2";
    public static final String INPUT_TYPE_TRANSACTION_ID = "1";
    public static final String DOWNLOAD_DATA_TYPE_PDF = "pdf";

    public static final int SIGN_TYPE_HSM_WITH_CKS = 2;
    /** Ký số sau loại hóa đơn MTT (MISA doc §1.6). */
    public static final int SIGN_TYPE_MTT = 5;

    public static final String HEADER_COMPANY_TAX_CODE = "CompanyTaxCode";

    public static final String OPERATION_AUTH_TOKEN = "auth/token";
    public static final String OPERATION_PUBLISH = "invoice/publish";
    public static final String OPERATION_GET_CERTIFICATES = "invoice/get-certificates";
    public static final String OPERATION_STATUS = "invoice/status";
    public static final String OPERATION_DOWNLOAD = "invoice/download";

    public static final String ERROR_PUBLISH_DISABLED =
            "MeInvoice publish is disabled. Set meinvoice.publish.enabled=true.";
    public static final String ERROR_APP_ID_REQUIRED = "meinvoice.credentials.app-id is required for publish API";
    public static final String ERROR_PUBLISH_TOKEN_MISSING =
            "MeInvoice publish token response missing bearer token in data/Data";
    public static final String ERROR_ORDER_NOT_READY_PUBLISH_FORMAT = "Order not ready for MeInvoice publish: %s";
    public static final String ERROR_DRAFT_DELETED_CANNOT_PUBLISH = "MEINVOICE_DRAFT_DELETED";
    public static final String ERROR_ALREADY_PUBLISHED = "MEINVOICE_ALREADY_PUBLISHED";
    public static final String ERROR_NO_DRAFT_REF = "MEINVOICE_NO_DRAFT_REF";
    public static final String ERROR_CERTIFICATE_SN_REQUIRED = "MeInvoice CertificateSN is not configured and get-certificates returned none";
    public static final String ERROR_PUBLISH_RESULT_EMPTY = "MeInvoice publish: empty publishInvoiceResult";
    public static final String ERROR_PUBLISH_MISSING_IDENTIFIERS_FORMAT =
            "MeInvoice publish: no TransactionID/InvNo for RefID %s (publish did not issue invoice code)";
    public static final String ERROR_PUBLISH_RESULT_PARSE_FORMAT = "MeInvoice publish: cannot parse publishInvoiceResult JSON string";
    public static final String ERROR_PUBLISH_GOT_CREATE_RESULT =
            "MeInvoice publish returned createInvoiceResult instead of publishInvoiceResult. "
                    + "Check meinvoice.publish.sign-type (HSM=2) or sign-type-mtt (MTT=5), not SignType 1.";

    public static final String ERROR_TRANSACTION_ID_REQUIRED = "transactionId is required";

    public static final String ERROR_PUBLISH_ITEM_FAILED_FORMAT = "MeInvoice publish failed for RefID %s: %s";
    public static final String ERROR_HTTP_POST_FORMAT = "MeInvoice POST %s HTTP %s: %s";
    public static final String ERROR_HTTP_GET_FORMAT = "MeInvoice GET %s HTTP %s: %s";
    public static final String ERROR_API_SUCCESS_FALSE_FORMAT = "MeInvoice %s returned success=false: %s";
    public static final String ERROR_PUBLISH_API_FAILED_FORMAT = "MeInvoice %s failed: %s — %s";

    public static final String ERROR_CODE_INVOICE_DUPLICATED = "InvoiceDuplicated";
    public static final String ERROR_CODE_INVOICE_NUMBER_NOT_CONTINUOUS = "InvoiceNumberNotCotinuous";
    public static final String ERROR_CODE_INVOICE_NOT_EXIST = "InvoiceNotExist";

    public static final String ERROR_DOWNLOAD_EMPTY_DATA =
            "MeInvoice download: no invoice data returned (empty data)";
    public static final String ERROR_DOWNLOAD_NO_PDF_IN_ARRAY =
            "MeInvoice download: no PDF in data array";
    public static final String ERROR_DOWNLOAD_ITEM_FORMAT = "MeInvoice download [%s]: %s";
    public static final String ERROR_DOWNLOAD_FALLBACK_FAILED_FORMAT =
            "MeInvoice V2 download failed (%s); fallback viewrefid also failed: %s";

    public static final String RESPONSE_FIELD_TRANSACTION_ID = "transactionId";
    public static final String RESPONSE_FIELD_INV_NO = "invNo";
    public static final String RESPONSE_FIELD_PUBLISHED = "meinvoicePublished";
    public static final String RESPONSE_FIELD_PUBLISHED_AT = "meinvoicePublishedAt";

    public static final String JSON_FIELD_TRANSACTION_ID = "TransactionID";
    public static final String JSON_FIELD_INV_NO = "InvNo";
    public static final String JSON_FIELD_ERROR_CODE = "ErrorCode";

    private MeinvoicePublishConstants() {
    }
}
