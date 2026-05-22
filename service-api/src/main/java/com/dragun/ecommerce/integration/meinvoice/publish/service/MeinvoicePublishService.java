package com.dragun.ecommerce.integration.meinvoice.publish.service;

import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.dragun.ecommerce.integration.meinvoice.MeinvoicePublishOptions;
import com.dragun.ecommerce.integration.meinvoice.config.MeinvoiceIntegrationConfig;
import com.dragun.ecommerce.integration.meinvoice.publish.MeinvoicePublishConstants;
import com.dragun.ecommerce.integration.meinvoice.publish.dto.MeinvoiceV2InvoiceData;
import com.dragun.ecommerce.integration.meinvoice.client.MeinvoiceApiErrorParser;
import com.dragun.ecommerce.integration.meinvoice.publish.client.MeinvoicePublishApiClient;
import com.dragun.ecommerce.integration.meinvoice.publish.client.MeinvoicePublishResultParser;
import com.dragun.ecommerce.integration.meinvoice.publish.client.MeinvoicePublishedPdfParser;
import com.dragun.ecommerce.integration.meinvoice.publish.client.MeinvoicePublishResultParser.PublishItemResult;
import com.dragun.ecommerce.integration.meinvoice.publish.dto.MeinvoicePublishInvoiceRequest;
import com.dragun.ecommerce.integration.meinvoice.publish.mapper.MeinvoiceV2InvoiceDataBuilder;
import com.dragun.ecommerce.integration.meinvoice.service.MeinvoiceInvoiceService;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeinvoicePublishService {

    private final MeinvoiceIntegrationConfig config;
    private final MeinvoicePublishApiClient publishApiClient;
    private final MeinvoiceInvoiceService meinvoiceInvoiceService;
    private final OrderRepository orderRepository;

    private final ConcurrentHashMap<String, Object> publishLocksByInvSeries = new ConcurrentHashMap<>();

    @Transactional
    public Map<String, Object> publishDraftInvoiceForOrder(String orderKey, boolean lookupByPancakeOrderId) {
        requirePublishEnabled();
        meinvoiceInvoiceService.requireIntegrationReady();

        Order order = meinvoiceInvoiceService.resolveOrderForIntegration(orderKey, lookupByPancakeOrderId);
        List<String> issues = collectPublishValidationIssues(order);
        if (!issues.isEmpty()) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoicePublishConstants.ERROR_ORDER_NOT_READY_PUBLISH_FORMAT,
                    issues));
        }

        String refId = order.getMeinvoiceRefId().trim();
        MeinvoiceV2InvoiceData invoiceData = MeinvoiceV2InvoiceDataBuilder.build(order, refId, config);

        String invSeries = config.getDefaults().getInvSeries();
        Object lock = publishLocksByInvSeries.computeIfAbsent(invSeries, key -> new Object());

        PublishItemResult itemResult;
        synchronized (lock) {
            itemResult = publishWithRetry(invoiceData);
            sleepSequentialDelay();
        }

        markOrderPublished(order, itemResult);
        syncInvoiceStatusQuietly(order);

        Map<String, Object> result = new HashMap<>();
        result.put("refId", refId);
        result.put("orderBusinessId", order.getOrderId());
        result.put(MeinvoicePublishConstants.RESPONSE_FIELD_TRANSACTION_ID, itemResult.transactionId());
        result.put(MeinvoicePublishConstants.RESPONSE_FIELD_INV_NO, itemResult.invNo());
        if (StringUtils.hasText(itemResult.invCode())) {
            result.put("invCode", itemResult.invCode());
        }
        result.put(MeinvoicePublishConstants.RESPONSE_FIELD_PUBLISHED, true);
        result.put(MeinvoicePublishConstants.RESPONSE_FIELD_PUBLISHED_AT, order.getMeinvoicePublishedAt());
        result.put("invoiceCalcu", MeinvoicePublishOptions.invoiceCalculatingMachine(config));
        result.put("signType", MeinvoicePublishOptions.signType(config));
        result.put("invSeries", invSeries);
        return result;
    }

    public Map<String, Object> syncPublishStatusForOrder(String orderKey, boolean lookupByPancakeOrderId) {
        requirePublishEnabled();
        Order order = meinvoiceInvoiceService.resolveOrderForIntegration(orderKey, lookupByPancakeOrderId);
        if (!StringUtils.hasText(order.getMeinvoiceRefId())) {
            throw new IllegalArgumentException(MeinvoiceIntegrationConstants.ERROR_REF_ID_REQUIRED);
        }
        List<String> statusKeys = new ArrayList<>();
        if (StringUtils.hasText(order.getMeinvoiceTransactionId())) {
            statusKeys.add(order.getMeinvoiceTransactionId().trim());
        } else if (StringUtils.hasText(order.getMeinvoiceRefId())) {
            statusKeys.add(order.getMeinvoiceRefId().trim());
        }
        JsonNode statusRoot = publishApiClient.getInvoiceStatus(
                config.getDefaults().isInvoiceWithCode(),
                MeinvoicePublishOptions.invoiceCalculatingMachine(config),
                StringUtils.hasText(order.getMeinvoiceTransactionId()),
                statusKeys);
        applyStatusFromResponse(order, statusRoot);

        Map<String, Object> result = new HashMap<>();
        result.put("orderBusinessId", order.getOrderId());
        result.put("refId", order.getMeinvoiceRefId());
        result.put("meinvoicePublished", order.getMeinvoicePublished());
        result.put("meinvoiceTransactionId", order.getMeinvoiceTransactionId());
        result.put("meinvoiceInvNo", order.getMeinvoiceInvNo());
        result.put("meinvoiceSendTaxStatus", order.getMeinvoiceSendTaxStatus());
        result.put("statusResponse", statusRoot);
        return result;
    }

    public byte[] downloadPublishedPdfBytes(String transactionId) {
        requirePublishEnabled();
        if (!StringUtils.hasText(transactionId)) {
            throw new IllegalArgumentException(MeinvoicePublishConstants.ERROR_TRANSACTION_ID_REQUIRED);
        }
        JsonNode root = publishApiClient.downloadPublishedPdf(
                config.getDefaults().isInvoiceWithCode(),
                MeinvoicePublishOptions.invoiceCalculatingMachine(config),
                transactionId.trim());
        assertApiSuccess(root, MeinvoicePublishConstants.OPERATION_DOWNLOAD);
        return MeinvoicePublishedPdfParser.parsePdfBytes(root);
    }

    /**
     * Published PDF via V2 download; optional {@code refId} fallback to V1 {@code GET /webapp/viewrefid}.
     */
    public Map<String, Object> previewPublishedPdfBase64(String transactionId, String refId) {
        byte[] pdf;
        boolean usedViewRefFallback = false;
        try {
            pdf = downloadPublishedPdfBytes(transactionId);
        } catch (IllegalStateException v2Error) {
            if (!StringUtils.hasText(refId)) {
                throw v2Error;
            }
            log.warn(
                    "MeInvoice V2 download failed for transactionId={}, trying viewrefid refId={}: {}",
                    transactionId,
                    refId,
                    v2Error.getMessage());
            try {
                pdf = meinvoiceInvoiceService.fetchInvoicePdfBytesByViewRefId(refId.trim(), null);
                usedViewRefFallback = true;
            } catch (Exception fallbackError) {
                throw new IllegalStateException(String.format(
                        Locale.ROOT,
                        MeinvoicePublishConstants.ERROR_DOWNLOAD_FALLBACK_FAILED_FORMAT,
                        v2Error.getMessage(),
                        fallbackError.getMessage()), v2Error);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put(MeinvoiceIntegrationConstants.RESPONSE_FIELD_PDF_BASE64, Base64.getEncoder().encodeToString(pdf));
        result.put("transactionId", transactionId.trim());
        if (usedViewRefFallback) {
            result.put("pdfSource", "viewrefid");
            result.put("refId", refId.trim());
        } else {
            result.put("pdfSource", "invoice-download");
        }
        return result;
    }

    private List<String> collectPublishValidationIssues(Order order) {
        List<String> issues = new ArrayList<>(meinvoiceInvoiceService.collectPublishValidationIssues(order));
        if (Boolean.TRUE.equals(order.getMeinvoiceDraftDeleted())) {
            issues.add(MeinvoiceIntegrationConstants.VALIDATION_MEINVOICE_DRAFT_DELETED);
        }
        if (Boolean.TRUE.equals(order.getMeinvoicePublished())) {
            issues.add(MeinvoiceIntegrationConstants.VALIDATION_MEINVOICE_ALREADY_PUBLISHED);
        }
        if (!StringUtils.hasText(order.getMeinvoiceRefId())) {
            issues.add(MeinvoiceIntegrationConstants.VALIDATION_MEINVOICE_NO_DRAFT_REF);
        }
        return issues;
    }

    private PublishItemResult publishWithRetry(MeinvoiceV2InvoiceData invoiceData) {
        int maxAttempts = 3;
        IllegalStateException lastError = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return callPublish(invoiceData);
            } catch (IllegalStateException ex) {
                lastError = ex;
                if (!containsRetryableMessage(ex.getMessage())) {
                    throw ex;
                }
                log.warn("MeInvoice publish attempt {}/{} failed: {}", attempt, maxAttempts, ex.getMessage());
                sleepSequentialDelay();
            }
        }
        if (lastError != null) {
            throw lastError;
        }
        throw new IllegalStateException(MeinvoicePublishConstants.ERROR_PUBLISH_RESULT_EMPTY);
    }

    private PublishItemResult callPublish(MeinvoiceV2InvoiceData invoiceData) {
        int signType = MeinvoicePublishOptions.signType(config);
        String certificateSn = signType == MeinvoicePublishConstants.SIGN_TYPE_HSM_WITH_CKS
                ? resolveCertificateSn()
                : null;
        log.info(
                "MeInvoice publish: invSeries={}, invoiceCalcu={}, signType={}",
                config.getDefaults().getInvSeries(),
                MeinvoicePublishOptions.invoiceCalculatingMachine(config),
                signType);
        MeinvoicePublishInvoiceRequest request = MeinvoicePublishInvoiceRequest.builder()
                .signType(signType)
                .certificateSn(certificateSn)
                .invoiceData(List.of(invoiceData))
                .publishInvoiceData(null)
                .build();

        JsonNode root = publishApiClient.publishInvoices(request);
        assertApiSuccess(root, MeinvoicePublishConstants.OPERATION_PUBLISH);
        return MeinvoicePublishResultParser.parseFirstItem(root);
    }

    private String resolveCertificateSn() {
        if (StringUtils.hasText(config.getPublish().getCertificateSn())) {
            return config.getPublish().getCertificateSn().trim();
        }
        JsonNode certs = publishApiClient.getCertificates();
        assertApiSuccess(certs, MeinvoicePublishConstants.OPERATION_GET_CERTIFICATES);
        JsonNode data = certs.path("data");
        if (data.isMissingNode() || data.isNull()) {
            data = certs.path("Data");
        }
        if (data.isArray() && !data.isEmpty()) {
            JsonNode first = data.get(0);
            String sn = first.path("CertificateSN").asText(null);
            if (!StringUtils.hasText(sn)) {
                sn = first.path("SerialNumber").asText(null);
            }
            if (StringUtils.hasText(sn)) {
                return sn.trim();
            }
        }
        if (data.isTextual() && StringUtils.hasText(data.asText())) {
            return parseCertificateSnFromText(data.asText());
        }
        throw new IllegalStateException(MeinvoicePublishConstants.ERROR_CERTIFICATE_SN_REQUIRED);
    }

    private static String parseCertificateSnFromText(String jsonText) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            JsonNode arr = mapper.readTree(jsonText);
            if (arr.isArray() && !arr.isEmpty()) {
                return arr.get(0).path("CertificateSN").asText(null);
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }

    private void markOrderPublished(Order order, PublishItemResult itemResult) {
        if (!StringUtils.hasText(itemResult.transactionId()) && !StringUtils.hasText(itemResult.invNo())) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoicePublishConstants.ERROR_PUBLISH_MISSING_IDENTIFIERS_FORMAT,
                    itemResult.refId()));
        }
        order.setMeinvoicePublished(true);
        order.setMeinvoicePublishedAt(LocalDateTime.now());
        order.setMeinvoiceTransactionId(itemResult.transactionId());
        order.setMeinvoiceInvNo(itemResult.invNo());
        order.setMeinvoicePublishErrorCode(null);
        orderRepository.save(order);
    }

    private void syncInvoiceStatusQuietly(Order order) {
        try {
            List<String> statusKeys = new ArrayList<>();
            if (StringUtils.hasText(order.getMeinvoiceTransactionId())) {
                statusKeys.add(order.getMeinvoiceTransactionId().trim());
            } else if (StringUtils.hasText(order.getMeinvoiceRefId())) {
                statusKeys.add(order.getMeinvoiceRefId().trim());
            } else {
                return;
            }
            JsonNode statusRoot = publishApiClient.getInvoiceStatus(
                    config.getDefaults().isInvoiceWithCode(),
                    MeinvoicePublishOptions.invoiceCalculatingMachine(config),
                    StringUtils.hasText(order.getMeinvoiceTransactionId()),
                    statusKeys);
            applyStatusFromResponse(order, statusRoot);
        } catch (Exception e) {
            log.warn("MeInvoice status sync after publish failed for order {}: {}",
                    order.getOrderId(), e.getMessage());
        }
    }

    private void applyStatusFromResponse(Order order, JsonNode statusRoot) {
        if (statusRoot == null) {
            return;
        }
        JsonNode data = statusRoot.path("data");
        if (data.isTextual() && StringUtils.hasText(data.asText())) {
            try {
                data = new com.fasterxml.jackson.databind.ObjectMapper().readTree(data.asText().trim());
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.warn("MeInvoice status: cannot parse data JSON string: {}", e.getMessage());
                return;
            }
        }
        if (!data.isArray() || data.isEmpty()) {
            return;
        }
        JsonNode first = data.get(0);
        String transactionId = first.path("TransactionID").asText(null);
        if (StringUtils.hasText(transactionId)) {
            order.setMeinvoiceTransactionId(transactionId);
        }
        String invNo = first.path("InvNo").asText(null);
        if (StringUtils.hasText(invNo)) {
            order.setMeinvoiceInvNo(invNo);
        }
        if (first.has("SendTaxStatus") && !first.get("SendTaxStatus").isNull()) {
            order.setMeinvoiceSendTaxStatus(first.get("SendTaxStatus").asInt());
        }
        if (first.has("PublishStatus") && first.get("PublishStatus").asInt() == 1) {
            order.setMeinvoicePublished(true);
        }
        orderRepository.save(order);
    }

    private void requirePublishEnabled() {
        if (!config.isEnabled() || !config.getPublish().isEnabled()) {
            throw new IllegalStateException(MeinvoicePublishConstants.ERROR_PUBLISH_DISABLED);
        }
    }

    private static void assertApiSuccess(JsonNode root, String operation) {
        if (root == null) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoicePublishConstants.ERROR_API_SUCCESS_FALSE_FORMAT,
                    operation,
                    "empty response"));
        }
        boolean ok = root.path(MeinvoiceIntegrationConstants.JSON_FIELD_SUCCESS).asBoolean(false)
                || root.path("Success").asBoolean(false);
        if (!ok) {
            String description = root.path("descriptionErrorCode").asText(null);
            if (!StringUtils.hasText(description)) {
                description = root.path(MeinvoiceIntegrationConstants.JSON_FIELD_ERROR_DESCRIPTION).asText(null);
            }
            if (StringUtils.hasText(description)) {
                String code = root.path("errorCode").asText("");
                throw new IllegalStateException(String.format(
                        Locale.ROOT,
                        MeinvoicePublishConstants.ERROR_PUBLISH_API_FAILED_FORMAT,
                        operation,
                        code,
                        description));
            }
            throw new IllegalStateException(MeinvoiceApiErrorParser.formatApiFailure(root, operation));
        }
    }

    private void sleepSequentialDelay() {
        long delay = Math.max(0L, config.getPublish().getSequentialDelayMs());
        if (delay <= 0L) {
            return;
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("MeInvoice publish delay interrupted", e);
        }
    }

    private static boolean containsRetryableMessage(String message) {
        if (!StringUtils.hasText(message)) {
            return false;
        }
        return message.contains(MeinvoicePublishConstants.ERROR_CODE_INVOICE_DUPLICATED)
                || message.contains(MeinvoicePublishConstants.ERROR_CODE_INVOICE_NUMBER_NOT_CONTINUOUS);
    }
}
