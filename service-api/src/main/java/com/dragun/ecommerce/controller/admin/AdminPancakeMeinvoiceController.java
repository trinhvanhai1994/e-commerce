package com.dragun.ecommerce.controller.admin;

import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.dragun.ecommerce.integration.meinvoice.config.MeinvoiceIntegrationConfig;
import com.dragun.ecommerce.integration.meinvoice.service.MeinvoiceInvoiceService;
import com.dragun.ecommerce.model.dto.response.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Test APIs: Pancake POS order (synced in Thiyen DB) → mapping preview → MeInvoice (MISA) draft/preview.
 */
@RestController
@RequestMapping({
        "/api/thiyen/admin/integration/pancake-meinvoice",
        "/api/dragun/admin/integration/pancake-meinvoice"
})
@RequiredArgsConstructor
public class AdminPancakeMeinvoiceController {

    private final MeinvoiceInvoiceService meinvoiceInvoiceService;
    private final MeinvoiceIntegrationConfig meinvoiceIntegrationConfig;

    /**
     * Danh sách đơn đã sync từ Pancake ({@code order_type=PANCAKE}) kèm trạng thái sẵn sàng xuất HĐ.
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> listPancakeOrders() {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    meinvoiceInvoiceService.listPancakeOrdersForInvoicing(),
                    "Pancake orders for invoicing"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Failed to list orders: " + e.getMessage()));
        }
    }

    /**
     * Xem mapping Thiyen order → payload MeInvoice (không gọi MISA).
     *
     * @param orderKey {@code orders.order_id} hoặc pancake id nếu {@code by=pancake}
     */
    @GetMapping("/orders/{orderKey}/mapping")
    public ResponseEntity<ApiResponse<Map<String, Object>>> mapping(
            @PathVariable String orderKey,
            @RequestParam(required = false, defaultValue = MeinvoiceIntegrationConstants.LOOKUP_BY_ORDER) String by) {
        try {
            boolean byPancake = MeinvoiceIntegrationConstants.LOOKUP_BY_PANCAKE.equalsIgnoreCase(by);
            Map<String, Object> data = meinvoiceInvoiceService.buildInvoiceMappingPreview(orderKey, byPancake);
            boolean ready = Boolean.TRUE.equals(((Map<?, ?>) data.get("validation")).get("ready"));
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(ready)
                    .data(data)
                    .message(ready ? "Mapping ready for MeInvoice" : "Mapping preview with validation issues")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Mapping failed: " + e.getMessage()));
        }
    }

    /**
     * Gọi MeInvoice {@code /webapp/preview} với payload đã map (không insert).
     */
    @PostMapping("/orders/{orderKey}/preview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewOnMeinvoice(
            @PathVariable String orderKey,
            @RequestParam(required = false, defaultValue = MeinvoiceIntegrationConstants.LOOKUP_BY_ORDER) String by) {
        try {
            boolean byPancake = MeinvoiceIntegrationConstants.LOOKUP_BY_PANCAKE.equalsIgnoreCase(by);
            Map<String, Object> data = meinvoiceInvoiceService.previewInvoiceOnMeinvoice(orderKey, byPancake);
            boolean ok = Boolean.TRUE.equals(data.get("meinvoiceSuccess"));
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(ok)
                    .data(data)
                    .message(ok ? "MeInvoice preview OK" : "MeInvoice preview returned success=false")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Preview failed: " + e.getMessage()));
        }
    }

    /**
     * Tạo hóa đơn nháp trên MeInvoice ({@code /webapp/insert}) và lưu {@code meinvoice_submissions}.
     */
    @PostMapping("/orders/{orderKey}/draft-invoice")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createDraftInvoice(
            @PathVariable String orderKey,
            @RequestParam(required = false, defaultValue = MeinvoiceIntegrationConstants.LOOKUP_BY_ORDER) String by) {
        try {
            boolean byPancake = MeinvoiceIntegrationConstants.LOOKUP_BY_PANCAKE.equalsIgnoreCase(by);
            Map<String, Object> data = meinvoiceInvoiceService.createDraftInvoiceForOrder(orderKey, byPancake);
            boolean recorded = Boolean.TRUE.equals(data.get("recordedSuccess"));
            return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                    .success(recorded)
                    .data(data)
                    .message(recorded ? "Draft invoice created on MeInvoice" : "MeInvoice insert failed; see meinvoiceResponse")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Draft invoice failed: " + e.getMessage()));
        }
    }

    @GetMapping("/orders/{orderKey}/submissions")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> submissions(
            @PathVariable String orderKey,
            @RequestParam(required = false, defaultValue = MeinvoiceIntegrationConstants.LOOKUP_BY_ORDER) String by) {
        try {
            boolean byPancake = MeinvoiceIntegrationConstants.LOOKUP_BY_PANCAKE.equalsIgnoreCase(by);
            return ResponseEntity.ok(ApiResponse.success(
                    meinvoiceInvoiceService.listSubmissionsForOrder(orderKey, byPancake),
                    "MeInvoice submissions for order"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Failed to list submissions: " + e.getMessage()));
        }
    }

    @PostMapping("/lookup-by-ref-ids")
    public ResponseEntity<ApiResponse<JsonNode>> lookupByRefIds(
            @RequestParam(required = false) Boolean invoiceWithCode,
            @RequestBody List<String> refIds) {
        try {
            boolean iwc = invoiceWithCode != null
                    ? invoiceWithCode
                    : meinvoiceIntegrationConfig.getDefaults().isInvoiceWithCode();
            JsonNode root = meinvoiceInvoiceService.lookupByRefIds(iwc, refIds);
            return ResponseEntity.ok(ApiResponse.success(root, "Lookup completed"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("Lookup failed: " + e.getMessage()));
        }
    }
}
