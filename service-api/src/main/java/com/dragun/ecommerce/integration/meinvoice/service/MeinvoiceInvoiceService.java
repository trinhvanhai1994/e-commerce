package com.dragun.ecommerce.integration.meinvoice.service;

import com.dragun.ecommerce.integration.meinvoice.client.MeinvoiceApiClient;
import com.dragun.ecommerce.integration.meinvoice.client.MeinvoicePreviewPdfParser;
import com.dragun.ecommerce.integration.meinvoice.config.MeinvoiceIntegrationConfig;
import com.dragun.ecommerce.integration.meinvoice.dto.MeinvoiceInvoiceData;
import com.dragun.ecommerce.integration.meinvoice.dto.MeinvoiceInvoiceDetail;
import com.dragun.ecommerce.integration.meinvoice.model.MeinvoiceSubmission;
import com.dragun.ecommerce.integration.meinvoice.repository.MeinvoiceSubmissionRepository;
import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.dragun.ecommerce.integration.pancake.PancakeCatalogConstants;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.OrderItem;
import com.dragun.ecommerce.model.entity.Product;
import com.dragun.ecommerce.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeinvoiceInvoiceService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final MeinvoiceIntegrationConfig config;
    private final MeinvoiceApiClient meinvoiceApiClient;
    private final OrderRepository orderRepository;
    private final MeinvoiceSubmissionRepository submissionRepository;
    private final ObjectMapper objectMapper;

    public List<Map<String, Object>> listPancakeOrdersForInvoicing() {
        List<Order> orders = orderRepository.findPancakeOrdersWithItems();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Order order : orders) {
            rows.add(summarizeOrderForInvoice(order));
        }
        return rows;
    }

    /**
     * Preview Thiyen order (from Pancake POS sync) → MeInvoice payload without calling MISA.
     */
    public Map<String, Object> buildInvoiceMappingPreview(String orderKey, boolean lookupByPancakeOrderId) {
        Order order = resolveOrder(orderKey, lookupByPancakeOrderId);
        String businessId = order.getOrderId();
        List<String> issues = collectInvoiceValidationIssues(order);
        String previewRefId = UUID.randomUUID().toString();
        MeinvoiceInvoiceData payload = buildInvoiceData(order, previewRefId);

        Map<String, Object> result = new HashMap<>();
        result.put("order", summarizeOrderForInvoice(order));
        result.put("lineItems", summarizeLineItems(order));
        result.put("meinvoicePayload", objectMapper.convertValue(payload, Map.class));
        result.put("previewRefId", previewRefId);
        result.put("validation", Map.of(
                "ready", issues.isEmpty(),
                "issues", issues
        ));
        result.put("latestSubmission", latestSubmissionSummary(businessId).orElse(null));
        result.put("meinvoiceConfig", Map.of(
                "enabled", config.isEnabled(),
                "templateConfigured", isTemplateConfigured(),
                "invoiceWithCode", config.getDefaults().isInvoiceWithCode(),
                "defaultVatRate", config.getDefaults().getDefaultVatRate()
        ));
        return result;
    }

    /**
     * Call MeInvoice {@code POST /webapp/preview} with mapped payload (no insert).
     */
    public Map<String, Object> previewInvoiceOnMeinvoice(String orderKey, boolean lookupByPancakeOrderId) {
        requireEnabledAndCredentials();
        requireTemplateConfigured();
        Order order = resolveOrder(orderKey, lookupByPancakeOrderId);
        List<String> issues = collectInvoiceValidationIssues(order);
        if (!issues.isEmpty()) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_ORDER_NOT_READY_PREVIEW_FORMAT,
                    issues));
        }

        String refId = UUID.randomUUID().toString();
        MeinvoiceInvoiceData invoiceData = buildInvoiceData(order, refId);
        JsonNode response = meinvoiceApiClient.previewInvoice(invoiceData);
        assertMeinvoiceApiSuccess(response, MeinvoiceIntegrationConstants.OPERATION_PREVIEW);

        Map<String, Object> result = new HashMap<>();
        result.put("refId", refId);
        result.put("orderBusinessId", order.getOrderId());
        result.put("meinvoiceSuccess", true);
        result.put("meinvoiceResponse", response);
        result.put("meinvoicePayload", objectMapper.convertValue(invoiceData, Map.class));
        result.put(
                MeinvoiceIntegrationConstants.RESPONSE_FIELD_PDF_BASE64,
                Base64.getEncoder().encodeToString(
                        MeinvoicePreviewPdfParser.parsePdfBytesFromApiJson(
                                response,
                                MeinvoiceIntegrationConstants.OPERATION_PREVIEW)));
        return result;
    }

    public List<Map<String, Object>> listSubmissionsForOrder(String orderKey, boolean lookupByPancakeOrderId) {
        Order order = resolveOrder(orderKey, lookupByPancakeOrderId);
        return submissionRepository.findByOrderBusinessIdOrderByCreatedAtDesc(order.getOrderId()).stream()
                .map(this::submissionToMap)
                .toList();
    }

    public Map<String, Object> testConnection() {
        requireEnabledAndCredentials();
        JsonNode root = fetchTemplates(config.getDefaults().isInvoiceWithCode());
        int templateCount = countTemplates(root);
        Map<String, Object> out = new HashMap<>();
        out.put("tokenOk", true);
        out.put("templateCount", templateCount);
        out.put("rawSuccess", true);
        return out;
    }

    public JsonNode fetchTemplates(boolean invoiceWithCode) {
        requireEnabledAndCredentials();
        JsonNode root = meinvoiceApiClient.fetchTemplates(invoiceWithCode);
        assertMeinvoiceApiSuccess(root, MeinvoiceIntegrationConstants.OPERATION_TEMPLATES);
        return root;
    }

    public JsonNode lookupByRefIds(boolean invoiceWithCode, List<String> refIds) {
        requireEnabledAndCredentials();
        if (refIds == null || refIds.isEmpty()) {
            throw new IllegalArgumentException(MeinvoiceIntegrationConstants.ERROR_REF_IDS_EMPTY);
        }
        if (refIds.size() > MeinvoiceIntegrationConstants.GET_LIST_MAX_REF_IDS) {
            throw new IllegalArgumentException(MeinvoiceIntegrationConstants.ERROR_REF_IDS_TOO_MANY);
        }
        for (String id : refIds) {
            if (!StringUtils.hasText(id)) {
                throw new IllegalArgumentException(MeinvoiceIntegrationConstants.ERROR_REF_IDS_BLANK_ENTRY);
            }
        }
        JsonNode root = meinvoiceApiClient.getInvoicesByRefIds(invoiceWithCode, refIds);
        assertMeinvoiceApiSuccess(root, MeinvoiceIntegrationConstants.OPERATION_GET_LIST);
        return root;
    }

    /**
     * PDF bytes via MeInvoice {@code POST /webapp/preview} (InvoiceData + RefID).
     */
    public byte[] fetchInvoicePdfBytesByPreview(String orderKey, boolean lookupByPancakeOrderId, String refId) {
        requireEnabledAndCredentials();
        requireTemplateConfigured();
        if (!StringUtils.hasText(refId)) {
            throw new IllegalArgumentException(MeinvoiceIntegrationConstants.ERROR_REF_ID_REQUIRED);
        }
        String trimmedRefId = refId.trim();
        Order order = resolveOrder(orderKey, lookupByPancakeOrderId);
        if (StringUtils.hasText(order.getMeinvoiceRefId())
                && !trimmedRefId.equals(order.getMeinvoiceRefId())) {
            throw new IllegalArgumentException(String.format(
                    Locale.ROOT,
                    "refId does not match order meinvoice_ref_id (orderId=%s)",
                    order.getOrderId()));
        }
        List<String> issues = collectInvoiceValidationIssues(order, false);
        if (!issues.isEmpty()) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_ORDER_NOT_READY_PREVIEW_FORMAT,
                    issues));
        }

        MeinvoiceInvoiceData invoiceData = buildInvoiceData(order, trimmedRefId);
        JsonNode response = meinvoiceApiClient.previewInvoice(invoiceData);
        assertMeinvoiceApiSuccess(response, MeinvoiceIntegrationConstants.OPERATION_PREVIEW);
        return MeinvoicePreviewPdfParser.parsePdfBytesFromApiJson(
                response,
                MeinvoiceIntegrationConstants.OPERATION_PREVIEW);
    }

    public Map<String, Object> viewInvoicePdfPreviewForOrder(
            String orderKey,
            boolean lookupByPancakeOrderId,
            String refId) {
        byte[] pdf = fetchInvoicePdfBytesByPreview(orderKey, lookupByPancakeOrderId, refId);
        Map<String, Object> result = new HashMap<>();
        result.put("refId", refId.trim());
        result.put("orderBusinessId", resolveOrder(orderKey, lookupByPancakeOrderId).getOrderId());
        result.put(MeinvoiceIntegrationConstants.RESPONSE_FIELD_PDF_BASE64, Base64.getEncoder().encodeToString(pdf));
        result.put("contentType", MeinvoiceIntegrationConstants.MIME_TYPE_APPLICATION_PDF);
        return result;
    }

    /**
     * PDF file bytes via MeInvoice {@code GET /webapp/viewrefid} (download).
     */
    public byte[] fetchInvoicePdfBytesByViewRefId(String refId, Boolean invoiceWithCode) {
        requireEnabledAndCredentials();
        if (!StringUtils.hasText(refId)) {
            throw new IllegalArgumentException(MeinvoiceIntegrationConstants.ERROR_REF_ID_REQUIRED);
        }
        boolean iwc = invoiceWithCode != null
                ? invoiceWithCode
                : config.getDefaults().isInvoiceWithCode();
        return meinvoiceApiClient.fetchInvoicePdfBytesByViewRefId(iwc, refId.trim());
    }

    /**
     * Delete unpublished draft on MeInvoice ({@code DELETE /webapp/delete}) and clear local order flags.
     */
    @Transactional
    public Map<String, Object> deleteDraftInvoiceByRefId(String refId, Boolean invoiceWithCode, String orderBusinessId) {
        requireEnabledAndCredentials();
        if (!StringUtils.hasText(refId)) {
            throw new IllegalArgumentException(MeinvoiceIntegrationConstants.ERROR_REF_ID_REQUIRED);
        }
        String trimmedRefId = refId.trim();
        boolean iwc = invoiceWithCode != null
                ? invoiceWithCode
                : config.getDefaults().isInvoiceWithCode();

        JsonNode response = meinvoiceApiClient.deleteDraft(iwc, trimmedRefId);
        assertMeinvoiceApiSuccess(response, MeinvoiceIntegrationConstants.OPERATION_DELETE);

        String resolvedOrderId = resolveOrderBusinessIdForDelete(trimmedRefId, orderBusinessId);
        if (StringUtils.hasText(resolvedOrderId)) {
            orderRepository.findByOrderId(resolvedOrderId).ifPresent(this::clearMeinvoiceOnOrder);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("refId", trimmedRefId);
        result.put("orderBusinessId", resolvedOrderId);
        result.put("meinvoiceSuccess", true);
        result.put("meinvoiceResponse", response);
        return result;
    }

    private static void assertMeinvoiceApiSuccess(JsonNode root, String operation) {
        if (!root.path(MeinvoiceIntegrationConstants.JSON_FIELD_SUCCESS).asBoolean(false)) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_API_SUCCESS_FALSE_FORMAT,
                    operation,
                    root));
        }
    }

  @Transactional
    public Map<String, Object> createDraftInvoiceForOrder(String orderKey) {
        return createDraftInvoiceForOrder(orderKey, false);
    }

    @Transactional
    public Map<String, Object> createDraftInvoiceForOrder(String orderKey, boolean lookupByPancakeOrderId) {
        requireEnabledAndCredentials();
        requireTemplateConfigured();

        Order order = resolveOrder(orderKey, lookupByPancakeOrderId);
        String orderBusinessId = order.getOrderId();
        List<String> issues = collectInvoiceValidationIssues(order);
        if (!issues.isEmpty()) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_ORDER_NOT_READY_INSERT_FORMAT,
                    issues));
        }

        assertOrderNotYetInvoiced(order);

        String refId = UUID.randomUUID().toString();
        MeinvoiceInvoiceData invoiceData = buildInvoiceData(order, refId);

        Map<String, Object> result = new HashMap<>();
        result.put("refId", refId);
        result.put("orderBusinessId", orderBusinessId);
        result.put("pancakeOrderId", order.getPancakeOrderId());

        try {
            JsonNode response = meinvoiceApiClient.insertInvoices(List.of(invoiceData));
            result.put("meinvoiceSuccess", response.path("success").asBoolean(false));
            result.put("meinvoiceResponse", response.toString());

            boolean ok = interpretInsertSuccess(response);
            submissionRepository.save(MeinvoiceSubmission.builder()
                    .refId(refId)
                    .orderBusinessId(orderBusinessId)
                    .success(ok)
                    .lastErrorCode(ok ? null : firstErrorCode(response))
                    .lastMessage(ok ? null : abbreviate(
                            response.toString(),
                            MeinvoiceIntegrationConstants.SUBMISSION_MESSAGE_MAX_LENGTH))
                    .build());
            if (ok) {
                markOrderInvoiced(order, refId);
                orderRepository.save(order);
            }
            result.put("recordedSuccess", ok);
            result.put("meinvoiceInvoiced", order.getMeinvoiceInvoiced());
            result.put("meinvoiceRefId", order.getMeinvoiceRefId());
            return result;
        } catch (RuntimeException e) {
            log.error("MeInvoice insert failed for order {}: {}", orderBusinessId, e.getMessage());
            submissionRepository.save(MeinvoiceSubmission.builder()
                    .refId(refId)
                    .orderBusinessId(orderBusinessId)
                    .success(false)
                    .lastErrorCode(MeinvoiceIntegrationConstants.SUBMISSION_ERROR_CODE_EXCEPTION)
                    .lastMessage(abbreviate(e.getMessage(), MeinvoiceIntegrationConstants.SUBMISSION_MESSAGE_MAX_LENGTH))
                    .build());
            throw e;
        }
    }

    private void requireEnabledAndCredentials() {
        if (!config.isEnabled()) {
            throw new IllegalStateException(MeinvoiceIntegrationConstants.ERROR_INTEGRATION_DISABLED);
        }
        if (!StringUtils.hasText(config.getCredentials().getTaxcode())
                || !StringUtils.hasText(config.getCredentials().getUsername())
                || !StringUtils.hasText(config.getCredentials().getPassword())) {
            throw new IllegalStateException(MeinvoiceIntegrationConstants.ERROR_CREDENTIALS_INCOMPLETE);
        }
    }

    private void requireTemplateConfigured() {
        if (!isTemplateConfigured()) {
            throw new IllegalStateException(MeinvoiceIntegrationConstants.ERROR_TEMPLATE_NOT_CONFIGURED);
        }
    }

    private boolean isTemplateConfigured() {
        return StringUtils.hasText(config.getDefaults().getInvoiceTemplateId())
                && StringUtils.hasText(config.getDefaults().getInvSeries());
    }

    private Order resolveOrder(String orderKey, boolean lookupByPancakeOrderId) {
        if (!StringUtils.hasText(orderKey)) {
            throw new IllegalArgumentException(MeinvoiceIntegrationConstants.ERROR_ORDER_KEY_REQUIRED);
        }
        String key = orderKey.trim();
        if (lookupByPancakeOrderId) {
            return orderRepository.findByPancakeOrderIdWithItems(key)
                    .orElseThrow(() -> new IllegalArgumentException(String.format(
                            Locale.ROOT,
                            MeinvoiceIntegrationConstants.ERROR_PANCAKE_ORDER_NOT_FOUND_FORMAT,
                            key)));
        }
        return orderRepository.findByOrderIdWithItems(key)
                .orElseGet(() -> orderRepository.findByPancakeOrderIdWithItems(key)
                        .orElseThrow(() -> new IllegalArgumentException(String.format(
                                Locale.ROOT,
                                MeinvoiceIntegrationConstants.ERROR_ORDER_NOT_FOUND_FORMAT,
                                key))));
    }

    private List<String> collectInvoiceValidationIssues(Order order) {
        return collectInvoiceValidationIssues(order, true);
    }

    /**
     * @param blockIfAlreadyInvoiced when {@code false}, allow preview PDF for orders that already have a draft (insert still blocked).
     */
    private List<String> collectInvoiceValidationIssues(Order order, boolean blockIfAlreadyInvoiced) {
        List<String> issues = new ArrayList<>();
        if (!config.isEnabled()) {
            issues.add(MeinvoiceIntegrationConstants.VALIDATION_MEINVOICE_DISABLED);
        }
        if (!isTemplateConfigured()) {
            issues.add(MeinvoiceIntegrationConstants.VALIDATION_MEINVOICE_TEMPLATE_NOT_CONFIGURED);
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            issues.add(MeinvoiceIntegrationConstants.VALIDATION_NO_LINE_ITEMS);
        } else {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product != null && PancakeCatalogConstants.isSystemPancakeProductId(product.getPancakeProductId())) {
                    issues.add(String.format(Locale.ROOT, "%s%s",
                            MeinvoiceIntegrationConstants.VALIDATION_UNMAPPED_CATALOG_LINE_PREFIX,
                            resolveLineItemDescription(item)));
                } else if (!StringUtils.hasText(resolveLineItemDescription(item))) {
                    issues.add(String.format(
                            Locale.ROOT,
                            MeinvoiceIntegrationConstants.VALIDATION_MISSING_ITEM_NAME_FORMAT,
                            MeinvoiceIntegrationConstants.VALIDATION_MISSING_ITEM_NAME,
                            product != null ? product.getId() : MeinvoiceIntegrationConstants.UNKNOWN_PRODUCT_ID));
                }
            }
        }
        if (!StringUtils.hasText(order.getCustomerName())) {
            issues.add(MeinvoiceIntegrationConstants.VALIDATION_MISSING_CUSTOMER_NAME);
        }
        if (!StringUtils.hasText(order.getCustomerPhone())) {
            issues.add(MeinvoiceIntegrationConstants.VALIDATION_MISSING_CUSTOMER_PHONE);
        }
        if (!StringUtils.hasText(order.getCustomerAddress())) {
            issues.add(MeinvoiceIntegrationConstants.VALIDATION_MISSING_CUSTOMER_ADDRESS);
        }
        if (blockIfAlreadyInvoiced && isOrderInvoicedOnMeinvoice(order)) {
            String ref = order.getMeinvoiceRefId();
            issues.add(StringUtils.hasText(ref)
                    ? String.format(Locale.ROOT, "%s:%s",
                    MeinvoiceIntegrationConstants.VALIDATION_MEINVOICE_ALREADY_INVOICED, ref)
                    : MeinvoiceIntegrationConstants.VALIDATION_MEINVOICE_ALREADY_INVOICED);
        }
        return issues;
    }

    /**
     * {@code true} when this order must not receive another MeInvoice insert (DB flag or legacy submission row).
     */
    public boolean isOrderInvoicedOnMeinvoice(Order order) {
        if (order == null) {
            return false;
        }
        if (Boolean.TRUE.equals(order.getMeinvoiceInvoiced())) {
            return true;
        }
        return submissionRepository
                .findFirstByOrderBusinessIdAndSuccessTrueOrderByCreatedAtDesc(order.getOrderId())
                .isPresent();
    }

    private void assertOrderNotYetInvoiced(Order order) {
        if (isOrderInvoicedOnMeinvoice(order)) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    MeinvoiceIntegrationConstants.ERROR_ORDER_ALREADY_INVOICED_FORMAT,
                    order.getMeinvoiceRefId(),
                    order.getOrderId()));
        }
    }

    private void markOrderInvoiced(Order order, String refId) {
        order.setMeinvoiceInvoiced(true);
        order.setMeinvoiceRefId(refId);
        order.setMeinvoiceInvoicedAt(LocalDateTime.now());
    }

    private void clearMeinvoiceOnOrder(Order order) {
        order.setMeinvoiceInvoiced(false);
        order.setMeinvoiceRefId(null);
        order.setMeinvoiceInvoicedAt(null);
        orderRepository.save(order);
    }

    private String resolveOrderBusinessIdForDelete(String refId, String orderBusinessId) {
        if (StringUtils.hasText(orderBusinessId)) {
            return orderBusinessId.trim();
        }
        return submissionRepository.findByRefId(refId)
                .map(MeinvoiceSubmission::getOrderBusinessId)
                .orElse(null);
    }

    private Map<String, Object> summarizeOrderForInvoice(Order order) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", order.getId());
        m.put("orderId", order.getOrderId());
        m.put("pancakeOrderId", order.getPancakeOrderId());
        m.put("orderType", order.getOrderType());
        m.put("status", order.getStatus() != null ? order.getStatus().name() : null);
        m.put("customerName", order.getCustomerName());
        m.put("customerPhone", order.getCustomerPhone());
        m.put("customerAddress", order.getCustomerAddress());
        m.put("paymentMethod", order.getPaymentMethod());
        m.put("subTotal", order.getSubTotal());
        m.put("shippingFee", order.getShippingFee());
        m.put("total", order.getTotal());
        m.put("itemCount", order.getItems() != null ? order.getItems().size() : 0);
        m.put("pancakeSyncedAt", order.getPancakeSyncedAt());
        m.put("pancakeImported", Boolean.TRUE.equals(order.getPancakeImported()));
        m.put("createdAt", order.getCreatedAt());
        m.put("meinvoiceInvoiced", isOrderInvoicedOnMeinvoice(order));
        m.put("meinvoiceRefId", order.getMeinvoiceRefId());
        m.put("meinvoiceInvoicedAt", order.getMeinvoiceInvoicedAt());
        List<String> issues = collectInvoiceValidationIssues(order);
        m.put("invoiceReady", issues.isEmpty());
        m.put("canCreateMeinvoice", !isOrderInvoicedOnMeinvoice(order)
                && issues.stream().noneMatch(i -> i.startsWith(
                        MeinvoiceIntegrationConstants.VALIDATION_MEINVOICE_ALREADY_INVOICED)));
        m.put("validationIssues", issues);
        return m;
    }

    private List<Map<String, Object>> summarizeLineItems(Order order) {
        if (order.getItems() == null) {
            return List.of();
        }
        List<Map<String, Object>> lines = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            Map<String, Object> line = new HashMap<>();
            line.put("productName", item.getProductName());
            if (item.getProduct() != null) {
                line.put("catalogProductName", item.getProduct().getName());
            }
            line.put("meinvoiceLineDescription", resolveLineItemDescription(item));
            line.put("quantity", item.getQuantity());
            line.put("price", item.getPrice());
            line.put("lineTotal", item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            if (item.getProduct() != null) {
                line.put("localProductId", item.getProduct().getId());
                line.put("pancakeProductId", item.getProduct().getPancakeProductId());
                line.put("unmappedPlaceholder", PancakeCatalogConstants.isSystemPancakeProductId(
                        item.getProduct().getPancakeProductId()));
            }
            lines.add(line);
        }
        return lines;
    }

    private Optional<Map<String, Object>> latestSubmissionSummary(String orderBusinessId) {
        return submissionRepository.findFirstByOrderBusinessIdOrderByCreatedAtDesc(orderBusinessId)
                .map(this::submissionToMap);
    }

    private Map<String, Object> submissionToMap(MeinvoiceSubmission s) {
        Map<String, Object> m = new HashMap<>();
        m.put("refId", s.getRefId());
        m.put("orderBusinessId", s.getOrderBusinessId());
        m.put("success", s.isSuccess());
        m.put("lastErrorCode", s.getLastErrorCode());
        m.put("lastMessage", s.getLastMessage());
        m.put("createdAt", s.getCreatedAt());
        return m;
    }

    private static int countTemplates(JsonNode root) {
        JsonNode data = root.path("data");
        if (data.isArray()) {
            return data.size();
        }
        return 0;
    }

    private static boolean interpretInsertSuccess(JsonNode root) {
        if (!root.path("success").asBoolean(false)) {
            return false;
        }
        if (isNonEmptyFailurePayload(root.get("error"))) {
            return false;
        }
        if (isNonEmptyFailurePayload(root.get("errors"))) {
            return false;
        }
        return true;
    }

    /**
     * MeInvoice may return {@code error} / {@code errors} as an array, a string, or an object.
     */
    private static boolean isNonEmptyFailurePayload(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return false;
        }
        if (node.isArray()) {
            return node.size() > 0;
        }
        if (node.isTextual()) {
            return !node.asText().isBlank();
        }
        if (node.isObject()) {
            return node.size() > 0;
        }
        return false;
    }

    private static String firstErrorCode(JsonNode root) {
        JsonNode err = root.get("error");
        if (err != null && err.isArray() && err.size() > 0) {
            return err.get(0).path("errorCode").asText(err.get(0).toString());
        }
        JsonNode errs = root.get("errors");
        if (errs != null && errs.isArray() && errs.size() > 0) {
            return errs.get(0).path("errorCode").asText(errs.get(0).toString());
        }
        return root.path("errorCode").asText(null);
    }

    private static String abbreviate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    /**
     * MeInvoice line title: prefer {@link Product#getName()} (catalog), then POS snapshot on {@link OrderItem}.
     */
    static String resolveLineItemDescription(OrderItem item) {
        if (item == null) {
            return null;
        }
        Product product = item.getProduct();
        if (product != null && StringUtils.hasText(product.getName())) {
            return product.getName().trim();
        }
        if (StringUtils.hasText(item.getProductName())) {
            return item.getProductName().trim();
        }
        return null;
    }

    private MeinvoiceInvoiceData buildInvoiceData(Order order, String refId) {
        var defaults = config.getDefaults();
        int vatRatePercent = defaults.getDefaultVatRate();
        validateVatRateForLines(vatRatePercent);
        String unitName = defaults.getDefaultUnitName();
        boolean priceExcludesVat = defaults.isAssumePricesExcludeVat();
        BigDecimal exchangeRate = BigDecimal.ONE;

        List<MeinvoiceInvoiceDetail> details = new ArrayList<>();
        int sort = 1;
        for (OrderItem item : order.getItems()) {
            BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
            BigDecimal unitPriceInput = item.getPrice().setScale(6, RoundingMode.HALF_UP);
            BigDecimal unitPrice = priceExcludesVat
                    ? unitPriceInput.setScale(4, RoundingMode.HALF_UP)
                    : unitPriceExcludingVat(unitPriceInput, vatRatePercent);
            BigDecimal amountOC = qty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
            BigDecimal vatAmountOC = computeVatAmount(amountOC, vatRatePercent);

            details.add(MeinvoiceInvoiceDetail.builder()
                    .inventoryItemType(0)
                    .description(resolveLineItemDescription(item))
                    .sortOrderView(sort)
                    .sortOrder(sort)
                    .isPromotion(false)
                    .unitName(unitName)
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .amountOC(amountOC)
                    .amount(scale2(amountOC.multiply(exchangeRate)))
                    .discountRate(BigDecimal.ZERO)
                    .discountAmountOC(BigDecimal.ZERO)
                    .discountAmount(BigDecimal.ZERO)
                    .vatRate(BigDecimal.valueOf(vatRatePercent))
                    .vatAmountOC(vatAmountOC)
                    .vatAmount(scale2(vatAmountOC.multiply(exchangeRate)))
                    .build());
            sort++;
        }

        if (order.getShippingFee() != null && order.getShippingFee().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal shippingInput = order.getShippingFee().setScale(6, RoundingMode.HALF_UP);
            BigDecimal amountOC = (priceExcludesVat
                    ? shippingInput
                    : unitPriceExcludingVat(shippingInput, vatRatePercent))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal vatAmountOC = computeVatAmount(amountOC, vatRatePercent);
            details.add(MeinvoiceInvoiceDetail.builder()
                    .inventoryItemType(0)
                    .description(MeinvoiceIntegrationConstants.SHIPPING_LINE_DESCRIPTION)
                    .sortOrderView(sort)
                    .sortOrder(sort)
                    .isPromotion(false)
                    .unitName(MeinvoiceIntegrationConstants.SHIPPING_LINE_UNIT_NAME)
                    .quantity(BigDecimal.ONE)
                    .unitPrice(amountOC)
                    .amountOC(amountOC)
                    .amount(scale2(amountOC.multiply(exchangeRate)))
                    .discountRate(BigDecimal.ZERO)
                    .discountAmountOC(BigDecimal.ZERO)
                    .discountAmount(BigDecimal.ZERO)
                    .vatRate(BigDecimal.valueOf(vatRatePercent))
                    .vatAmountOC(vatAmountOC)
                    .vatAmount(scale2(vatAmountOC.multiply(exchangeRate)))
                    .build());
        }

        BigDecimal totalSaleAmountOC = BigDecimal.ZERO;
        BigDecimal totalVatAmountOC = BigDecimal.ZERO;
        for (MeinvoiceInvoiceDetail d : details) {
            if (Objects.equals(0, d.getInventoryItemType())) {
                totalSaleAmountOC = totalSaleAmountOC.add(d.getAmountOC());
                totalVatAmountOC = totalVatAmountOC.add(d.getVatAmountOC());
            }
        }
        BigDecimal totalDiscountOC = BigDecimal.ZERO;
        BigDecimal totalAmountOC = totalSaleAmountOC.subtract(totalDiscountOC).add(totalVatAmountOC);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime orderTs = order.getCreatedAt() != null ? order.getCreatedAt() : now;

        BigDecimal masterVatRate = BigDecimal.valueOf(vatRatePercent);

        return MeinvoiceInvoiceData.builder()
                .refId(refId)
                .invoiceTemplateId(defaults.getInvoiceTemplateId())
                .invSeries(defaults.getInvSeries())
                .invNo(MeinvoiceIntegrationConstants.INV_NO_PLACEHOLDER)
                .invDate(formatInvDate(orderTs))
                .accountObjectName(order.getCustomerName())
                .accountObjectAddress(order.getCustomerAddress())
                .contactName(order.getCustomerName())
                .receiverMobile(order.getCustomerPhone())
                .paymentMethod(mapPaymentMethod(order.getPaymentMethod()))
                .currencyCode(defaults.getCurrencyCode())
                .currencyId(defaults.getCurrencyCode())
                .vatRate(masterVatRate)
                .discountRate(BigDecimal.ZERO)
                .exchangeRate(exchangeRate)
                .totalSaleAmountOC(scale2(totalSaleAmountOC))
                .totalSaleAmount(scale2(totalSaleAmountOC.multiply(exchangeRate)))
                .totalDiscountAmountOC(scale2(totalDiscountOC))
                .totalDiscountAmount(scale2(totalDiscountOC.multiply(exchangeRate)))
                .totalVatAmountOC(scale2(totalVatAmountOC))
                .totalVatAmount(scale2(totalVatAmountOC.multiply(exchangeRate)))
                .totalAmountOC(scale2(totalAmountOC))
                .totalAmount(scale2(totalAmountOC.multiply(exchangeRate)))
                .createdDate(formatInvDate(now))
                .modifiedDate(formatInvDate(now))
                .invoiceDetails(details)
                .build();
    }

    /** Postman uses {@code InvDate} with offset e.g. {@code 2025-09-26T00:00:00+07:00}. */
    private static String formatInvDate(LocalDateTime local) {
        OffsetDateTime odt = local.atZone(VN_ZONE).toOffsetDateTime();
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(odt);
    }

    private static void validateVatRateForLines(int vatRatePercent) {
        if (vatRatePercent < -3 || vatRatePercent > 100) {
            throw new IllegalStateException(
                    "meinvoice.defaults.default-vat-rate must be between -3 and 100 (MeInvoice line VATRate semantics).");
        }
    }

    /**
     * Converts a VAT-inclusive unit amount to exclusive VAT using rate R%: net = gross * 100 / (100 + R).
     */
    private static BigDecimal unitPriceExcludingVat(BigDecimal grossAmount, int vatRatePercent) {
        if (vatRatePercent <= 0) {
            return grossAmount.setScale(4, RoundingMode.HALF_UP);
        }
        BigDecimal hundred = BigDecimal.valueOf(100);
        BigDecimal divisor = hundred.add(BigDecimal.valueOf(vatRatePercent));
        return grossAmount.multiply(hundred).divide(divisor, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal computeVatAmount(BigDecimal amountBeforeVat, int vatRatePercent) {
        if (vatRatePercent < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amountBeforeVat
                .multiply(BigDecimal.valueOf(vatRatePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal scale2(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Postman insert sample uses {@code "TM/CK"}; vé sample uses {@code "Tiền mặt"}.
     */
    private static String mapPaymentMethod(String paymentMethod) {
        if (!StringUtils.hasText(paymentMethod)) {
            return MeinvoiceIntegrationConstants.PAYMENT_METHOD_CASH_OR_TRANSFER;
        }
        String upperPaymentMethod = paymentMethod.toUpperCase(Locale.ROOT);
        if (upperPaymentMethod.contains("COD") || upperPaymentMethod.contains("CASH")
                || upperPaymentMethod.contains("TIỀN MẶT")) {
            return MeinvoiceIntegrationConstants.PAYMENT_METHOD_CASH;
        }
        if (upperPaymentMethod.contains("CK") || upperPaymentMethod.contains("CHUYỂN")
                || upperPaymentMethod.contains("TRANSFER") || upperPaymentMethod.contains("VIETQR")) {
            return MeinvoiceIntegrationConstants.PAYMENT_METHOD_TRANSFER;
        }
        if (upperPaymentMethod.contains("TM")) {
            return MeinvoiceIntegrationConstants.PAYMENT_METHOD_CASH_OR_TRANSFER;
        }
        return MeinvoiceIntegrationConstants.PAYMENT_METHOD_CASH_OR_TRANSFER;
    }
}
