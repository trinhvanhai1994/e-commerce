package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.dragun.ecommerce.integration.meinvoice.config.MeinvoiceIntegrationConfig;
import com.dragun.ecommerce.integration.meinvoice.publish.MeinvoicePublishConstants;
import com.dragun.ecommerce.integration.meinvoice.publish.service.MeinvoicePublishService;
import com.dragun.ecommerce.integration.meinvoice.service.MeinvoiceInvoiceService;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping({
        "/api/dragun/admin/integration/meinvoice",
        "/api/thiyen/admin/integration/meinvoice"
})
@RequiredArgsConstructor
public class AdminMeinvoiceController {

    private static final String MESSAGE_CONNECTION_OK = "MeInvoice connection OK";
    private static final String MESSAGE_CONNECTION_FAILED_FORMAT = "MeInvoice connection failed: %s";
    private static final String MESSAGE_TEMPLATES_OK = "Templates retrieved";
    private static final String MESSAGE_TEMPLATES_FAILED_FORMAT = "Failed to load templates: %s";
    private static final String MESSAGE_DRAFT_CREATED = "Draft invoice created on MeInvoice";
    private static final String MESSAGE_DRAFT_ERRORS = "MeInvoice returned errors; see meinvoiceResponse";
    private static final String MESSAGE_DRAFT_FAILED_FORMAT = "Draft invoice failed: %s";
    private static final String MESSAGE_PDF_FAILED_FORMAT = "Failed to load invoice PDF: %s";
    private static final String MESSAGE_PDF_OK = "Invoice PDF loaded";
    private static final String MESSAGE_LOOKUP_OK = "Lookup completed";
    private static final String MESSAGE_LOOKUP_FAILED_FORMAT = "Lookup failed: %s";
    private static final String MESSAGE_DELETE_OK = "Draft invoice deleted on MeInvoice";
    private static final String MESSAGE_DELETE_FAILED_FORMAT = "Delete draft invoice failed: %s";
    private static final String MESSAGE_PUBLISH_OK = "Invoice published on MeInvoice";
    private static final String MESSAGE_PUBLISH_FAILED_FORMAT = "Publish invoice failed: %s";
    private static final String MESSAGE_STATUS_OK = "Invoice status synced";
    private static final String MESSAGE_STATUS_FAILED_FORMAT = "Failed to sync invoice status: %s";

    private final MeinvoiceInvoiceService meinvoiceInvoiceService;
    private final MeinvoicePublishService meinvoicePublishService;
    private final MeinvoiceIntegrationConfig meinvoiceIntegrationConfig;

    @PostMapping("/test-connection")
    public ResponseEntity<ApiResponse<Map<String, Object>>> testConnection() {
        try {
            Map<String, Object> data = meinvoiceInvoiceService.testConnection();
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(data)
                    .message(MESSAGE_CONNECTION_OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(String.format(Locale.ROOT, MESSAGE_CONNECTION_FAILED_FORMAT, e.getMessage()))
                    .build());
        }
    }

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<JsonNode>> templates(
            @RequestParam(required = false) Boolean invoiceWithCode) {
        try {
            JsonNode root = meinvoiceInvoiceService.fetchTemplates(resolveInvoiceWithCode(invoiceWithCode));
            return ResponseEntity.ok(ApiResponse.<JsonNode>builder()
                    .success(true)
                    .data(root)
                    .message(MESSAGE_TEMPLATES_OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<JsonNode>builder()
                    .success(false)
                    .message(String.format(Locale.ROOT, MESSAGE_TEMPLATES_FAILED_FORMAT, e.getMessage()))
                    .build());
        }
    }

    @PostMapping("/orders/{orderId}/draft-invoice")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createDraftInvoice(
            @PathVariable String orderId,
            @RequestParam(required = false, defaultValue = MeinvoiceIntegrationConstants.LOOKUP_BY_ORDER) String by) {
        try {
            Map<String, Object> data = meinvoiceInvoiceService.createDraftInvoiceForOrder(
                    orderId,
                    isLookupByPancake(by));
            boolean recorded = Boolean.TRUE.equals(data.get("recordedSuccess"));
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(recorded)
                    .data(data)
                    .message(recorded ? MESSAGE_DRAFT_CREATED : MESSAGE_DRAFT_ERRORS)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(String.format(Locale.ROOT, MESSAGE_DRAFT_FAILED_FORMAT, e.getMessage()))
                    .build());
        }
    }

    @DeleteMapping("/invoices")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteDraftInvoice(
            @RequestParam String refId,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Boolean invoiceWithCode) {
        if (!StringUtils.hasText(refId)) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(MeinvoiceIntegrationConstants.ERROR_REF_ID_REQUIRED)
                    .build());
        }
        try {
            Map<String, Object> data = meinvoiceInvoiceService.deleteDraftInvoiceByRefId(
                    refId,
                    invoiceWithCode,
                    orderId);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(data)
                    .message(MESSAGE_DELETE_OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(String.format(Locale.ROOT, MESSAGE_DELETE_FAILED_FORMAT, e.getMessage()))
                    .build());
        }
    }

    /**
     * Preview PDF via MeInvoice {@code POST /webapp/preview} (base64 in JSON).
     */
    @PostMapping("/orders/{orderId}/invoice-preview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewInvoicePdfForOrder(
            @PathVariable String orderId,
            @RequestParam String refId,
            @RequestParam(required = false, defaultValue = MeinvoiceIntegrationConstants.LOOKUP_BY_ORDER) String by) {
        if (!StringUtils.hasText(refId)) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(MeinvoiceIntegrationConstants.ERROR_REF_ID_REQUIRED)
                    .build());
        }
        try {
            Map<String, Object> data = meinvoiceInvoiceService.viewInvoicePdfPreviewForOrder(
                    orderId,
                    isLookupByPancake(by),
                    refId);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(data)
                    .message(MESSAGE_PDF_OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(String.format(Locale.ROOT, MESSAGE_PDF_FAILED_FORMAT, e.getMessage()))
                    .build());
        }
    }

    /**
     * Download PDF via MeInvoice {@code GET /webapp/viewrefid}.
     */
    @GetMapping(value = "/invoices/pdf", produces = MeinvoiceIntegrationConstants.MIME_TYPE_APPLICATION_PDF)
    public ResponseEntity<?> downloadInvoicePdf(
            @RequestParam String refId,
            @RequestParam(required = false) Boolean invoiceWithCode) {
        if (!StringUtils.hasText(refId)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(MeinvoiceIntegrationConstants.ERROR_REF_ID_REQUIRED)
                            .build());
        }
        try {
            byte[] pdf = meinvoiceInvoiceService.fetchInvoicePdfBytesByViewRefId(refId, invoiceWithCode);
            String safeName = refId.replaceAll(
                    MeinvoiceIntegrationConstants.REF_ID_FILENAME_SAFE_PATTERN,
                    "_");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(MeinvoiceIntegrationConstants.MIME_TYPE_APPLICATION_PDF))
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format(
                            Locale.ROOT,
                            MeinvoiceIntegrationConstants.CONTENT_DISPOSITION_ATTACHMENT_FILENAME_FORMAT,
                            safeName))
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message(String.format(Locale.ROOT, MESSAGE_PDF_FAILED_FORMAT, e.getMessage()))
                    .build());
        }
    }

    @PostMapping("/orders/{orderId}/publish-invoice")
    public ResponseEntity<ApiResponse<Map<String, Object>>> publishInvoice(
            @PathVariable String orderId,
            @RequestParam(required = false, defaultValue = MeinvoiceIntegrationConstants.LOOKUP_BY_ORDER) String by) {
        try {
            Map<String, Object> data = meinvoicePublishService.publishDraftInvoiceForOrder(
                    orderId,
                    isLookupByPancake(by));
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(data)
                    .message(MESSAGE_PUBLISH_OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(String.format(Locale.ROOT, MESSAGE_PUBLISH_FAILED_FORMAT, e.getMessage()))
                    .build());
        }
    }

    @PostMapping("/orders/{orderId}/invoice-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> syncInvoiceStatus(
            @PathVariable String orderId,
            @RequestParam(required = false, defaultValue = MeinvoiceIntegrationConstants.LOOKUP_BY_ORDER) String by) {
        try {
            Map<String, Object> data = meinvoicePublishService.syncPublishStatusForOrder(
                    orderId,
                    isLookupByPancake(by));
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(data)
                    .message(MESSAGE_STATUS_OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(String.format(Locale.ROOT, MESSAGE_STATUS_FAILED_FORMAT, e.getMessage()))
                    .build());
        }
    }

    /**
     * Preview published PDF for admin popup (base64 JSON — same auth path as {@code invoice-preview}).
     */
    @PostMapping("/invoices/published-preview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewPublishedInvoicePdf(
            @RequestParam String transactionId,
            @RequestParam(required = false) String refId) {
        if (!StringUtils.hasText(transactionId)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message(MeinvoicePublishConstants.ERROR_TRANSACTION_ID_REQUIRED)
                            .build());
        }
        try {
            Map<String, Object> data = meinvoicePublishService.previewPublishedPdfBase64(
                    transactionId.trim(),
                    refId);
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(true)
                    .data(data)
                    .message(MESSAGE_PDF_OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message(String.format(Locale.ROOT, MESSAGE_PDF_FAILED_FORMAT, e.getMessage()))
                    .build());
        }
    }

    /**
     * Download published PDF via V2 {@code POST /invoice/download} (requires {@code transactionId}).
     */
    @GetMapping(value = "/invoices/published-pdf", produces = MeinvoiceIntegrationConstants.MIME_TYPE_APPLICATION_PDF)
    public ResponseEntity<?> downloadPublishedInvoicePdf(@RequestParam String transactionId) {
        if (!StringUtils.hasText(transactionId)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.builder()
                            .success(false)
                            .message(MeinvoicePublishConstants.ERROR_TRANSACTION_ID_REQUIRED)
                            .build());
        }
        try {
            byte[] pdf = meinvoicePublishService.downloadPublishedPdfBytes(transactionId);
            String safeName = transactionId.replaceAll(
                    MeinvoiceIntegrationConstants.REF_ID_FILENAME_SAFE_PATTERN,
                    "_");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(MeinvoiceIntegrationConstants.MIME_TYPE_APPLICATION_PDF))
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format(
                            Locale.ROOT,
                            MeinvoiceIntegrationConstants.CONTENT_DISPOSITION_ATTACHMENT_FILENAME_FORMAT,
                            safeName))
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(false)
                    .message(String.format(Locale.ROOT, MESSAGE_PDF_FAILED_FORMAT, e.getMessage()))
                    .build());
        }
    }

    @PostMapping("/lookup-by-ref-ids")
    public ResponseEntity<ApiResponse<JsonNode>> lookupByRefIds(
            @RequestParam(required = false) Boolean invoiceWithCode,
            @RequestBody List<String> refIds) {
        try {
            JsonNode root = meinvoiceInvoiceService.lookupByRefIds(resolveInvoiceWithCode(invoiceWithCode), refIds);
            return ResponseEntity.ok(ApiResponse.<JsonNode>builder()
                    .success(true)
                    .data(root)
                    .message(MESSAGE_LOOKUP_OK)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<JsonNode>builder()
                    .success(false)
                    .message(String.format(Locale.ROOT, MESSAGE_LOOKUP_FAILED_FORMAT, e.getMessage()))
                    .build());
        }
    }

    private boolean resolveInvoiceWithCode(Boolean invoiceWithCode) {
        return invoiceWithCode != null
                ? invoiceWithCode
                : meinvoiceIntegrationConfig.getDefaults().isInvoiceWithCode();
    }

    private static boolean isLookupByPancake(String by) {
        return MeinvoiceIntegrationConstants.isLookupByPancake(by);
    }
}
