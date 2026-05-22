package com.dragun.ecommerce.integration.meinvoice.publish.mapper;

import com.dragun.ecommerce.integration.meinvoice.MeinvoiceIntegrationConstants;
import com.dragun.ecommerce.integration.meinvoice.MeinvoiceVatMath;
import com.dragun.ecommerce.integration.meinvoice.config.MeinvoiceIntegrationConfig;
import com.dragun.ecommerce.integration.meinvoice.dto.MeinvoiceOriginalInvoiceDetail;
import com.dragun.ecommerce.integration.meinvoice.dto.MeinvoiceTaxRateInfo;
import com.dragun.ecommerce.integration.meinvoice.publish.MeinvoicePublishConstants;
import com.dragun.ecommerce.integration.meinvoice.publish.dto.MeinvoiceV2InvoiceData;
import com.dragun.ecommerce.model.entity.Order;
import com.dragun.ecommerce.model.entity.OrderItem;
import com.dragun.ecommerce.model.entity.Product;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds V2 {@link MeinvoiceV2InvoiceData} from an order (MISA doc §14.3–14.5).
 * Separate from V1 {@code /webapp/insert} — field names and shapes differ.
 */
public final class MeinvoiceV2InvoiceDataBuilder {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter V2_INV_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private MeinvoiceV2InvoiceDataBuilder() {
    }

    public static MeinvoiceV2InvoiceData build(
            Order order,
            String refId,
            MeinvoiceIntegrationConfig config) {
        var defaults = config.getDefaults();
        int vatRatePercent = defaults.getDefaultVatRate();
        MeinvoiceVatMath.validateVatRatePercent(vatRatePercent);
        String unitName = defaults.getDefaultUnitName();
        boolean priceExcludesVat = defaults.isAssumePricesExcludeVat();
        BigDecimal exchangeRate = BigDecimal.ONE;

        List<MeinvoiceOriginalInvoiceDetail> lines = new ArrayList<>();
        int sort = 1;
        for (OrderItem item : order.getItems()) {
            lines.add(buildLine(item, sort++, unitName, vatRatePercent, priceExcludesVat, exchangeRate));
        }
        if (order.getShippingFee() != null && order.getShippingFee().compareTo(BigDecimal.ZERO) > 0) {
            lines.add(buildShippingLine(order.getShippingFee(), sort, vatRatePercent, priceExcludesVat, exchangeRate));
        }

        BigDecimal totalSaleAmountOC = BigDecimal.ZERO;
        BigDecimal totalVatAmountOC = BigDecimal.ZERO;
        for (MeinvoiceOriginalInvoiceDetail line : lines) {
            if (line.getItemType() != null && line.getItemType() == MeinvoicePublishConstants.ITEM_TYPE_NORMAL_GOODS) {
                totalSaleAmountOC = totalSaleAmountOC.add(line.getAmountOC());
                totalVatAmountOC = totalVatAmountOC.add(line.getVatAmountOC());
            }
        }
        BigDecimal totalDiscountOC = BigDecimal.ZERO;
        BigDecimal totalAmountWithoutVatOC = totalSaleAmountOC.subtract(totalDiscountOC);
        BigDecimal totalAmountOC = totalAmountWithoutVatOC.add(totalVatAmountOC);

        // Publish date = today (VN): MISA rejects InvDate before last published invoice on same InvSeries (InvalidInvoiceDate).
        LocalDate publishInvDate = LocalDate.now(VN_ZONE);
        String vatRateName = MeinvoiceVatMath.formatVatRateName(BigDecimal.valueOf(vatRatePercent));

        return MeinvoiceV2InvoiceData.builder()
                .refId(refId)
                .invSeries(defaults.getInvSeries())
                .invDate(formatV2InvDate(publishInvDate))
                .currencyCode(defaults.getCurrencyCode())
                .exchangeRate(exchangeRate)
                .paymentMethodName(mapPaymentMethodName(order.getPaymentMethod()))
                .isInvoiceSummary(false)
                .isSendEmail(config.getPublish().isSendEmail())
                .buyerFullName(order.getCustomerName())
                .buyerAddress(order.getCustomerAddress())
                .buyerPhoneNumber(order.getCustomerPhone())
                .buyerOrderCode(order.getOrderId())
                .totalSaleAmountOC(MeinvoiceVatMath.scale2(totalSaleAmountOC))
                .totalSaleAmount(MeinvoiceVatMath.scale2(totalSaleAmountOC.multiply(exchangeRate)))
                .totalDiscountAmountOC(MeinvoiceVatMath.scale2(totalDiscountOC))
                .totalDiscountAmount(MeinvoiceVatMath.scale2(totalDiscountOC.multiply(exchangeRate)))
                .totalAmountWithoutVatOC(MeinvoiceVatMath.scale2(totalAmountWithoutVatOC))
                .totalAmountWithoutVat(MeinvoiceVatMath.scale2(totalAmountWithoutVatOC.multiply(exchangeRate)))
                .totalVatAmountOC(MeinvoiceVatMath.scale2(totalVatAmountOC))
                .totalVatAmount(MeinvoiceVatMath.scale2(totalVatAmountOC.multiply(exchangeRate)))
                .totalAmountOC(MeinvoiceVatMath.scale2(totalAmountOC))
                .totalAmount(MeinvoiceVatMath.scale2(totalAmountOC.multiply(exchangeRate)))
                .originalInvoiceDetail(lines)
                .taxRateInfo(List.of(MeinvoiceTaxRateInfo.builder()
                        .vatRateName(vatRateName)
                        .amountWithoutVatOC(MeinvoiceVatMath.scale2(totalAmountWithoutVatOC))
                        .vatAmountOC(MeinvoiceVatMath.scale2(totalVatAmountOC))
                        .build()))
                .build();
    }

    private static MeinvoiceOriginalInvoiceDetail buildLine(
            OrderItem item,
            int sort,
            String unitName,
            int vatRatePercent,
            boolean priceExcludesVat,
            BigDecimal exchangeRate) {
        BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
        BigDecimal unitPriceInput = item.getPrice().setScale(6, RoundingMode.HALF_UP);
        BigDecimal unitPrice = priceExcludesVat
                ? unitPriceInput.setScale(4, RoundingMode.HALF_UP)
                : MeinvoiceVatMath.unitPriceExcludingVat(unitPriceInput, vatRatePercent);
        BigDecimal amountOC = qty.multiply(unitPrice).setScale(2, RoundingMode.HALF_UP);
        BigDecimal vatAmountOC = MeinvoiceVatMath.computeVatAmount(amountOC, vatRatePercent);
        return toOriginalLine(
                resolveLineItemDescription(item),
                null,
                unitName,
                qty,
                unitPrice,
                amountOC,
                exchangeRate,
                vatRatePercent,
                vatAmountOC,
                sort,
                MeinvoicePublishConstants.ITEM_TYPE_NORMAL_GOODS);
    }

    private static MeinvoiceOriginalInvoiceDetail buildShippingLine(
            BigDecimal shippingFee,
            int sort,
            int vatRatePercent,
            boolean priceExcludesVat,
            BigDecimal exchangeRate) {
        BigDecimal shippingInput = shippingFee.setScale(6, RoundingMode.HALF_UP);
        BigDecimal amountOC = (priceExcludesVat
                ? shippingInput
                : MeinvoiceVatMath.unitPriceExcludingVat(shippingInput, vatRatePercent))
                    .setScale(2, RoundingMode.HALF_UP);
        BigDecimal vatAmountOC = MeinvoiceVatMath.computeVatAmount(amountOC, vatRatePercent);
        return toOriginalLine(
                MeinvoiceIntegrationConstants.SHIPPING_LINE_DESCRIPTION,
                null,
                MeinvoiceIntegrationConstants.SHIPPING_LINE_UNIT_NAME,
                BigDecimal.ONE,
                amountOC,
                amountOC,
                exchangeRate,
                vatRatePercent,
                vatAmountOC,
                sort,
                MeinvoicePublishConstants.ITEM_TYPE_NORMAL_GOODS);
    }

    private static MeinvoiceOriginalInvoiceDetail toOriginalLine(
            String itemName,
            String itemCode,
            String unitName,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal amountOC,
            BigDecimal exchangeRate,
            int vatRatePercent,
            BigDecimal vatAmountOC,
            int sort,
            int itemType) {
        BigDecimal amount = MeinvoiceVatMath.scale2(amountOC.multiply(exchangeRate));
        return MeinvoiceOriginalInvoiceDetail.builder()
                .itemType(itemType)
                .sortOrder(sort)
                .lineNumber(sort)
                .itemCode(itemCode)
                .itemName(itemName)
                .unitName(unitName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .amountOC(amountOC)
                .amount(amount)
                .discountRate(BigDecimal.ZERO)
                .discountAmountOC(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .amountWithoutVatOC(amountOC)
                .amountWithoutVat(amount)
                .vatRateName(MeinvoiceVatMath.formatVatRateName(BigDecimal.valueOf(vatRatePercent)))
                .vatAmountOC(vatAmountOC)
                .vatAmount(MeinvoiceVatMath.scale2(vatAmountOC.multiply(exchangeRate)))
                .build();
    }

    private static String resolveLineItemDescription(OrderItem item) {
        if (StringUtils.hasText(item.getProductName())) {
            return item.getProductName().trim();
        }
        Product product = item.getProduct();
        if (product != null && StringUtils.hasText(product.getName())) {
            return product.getName().trim();
        }
        return null;
    }

    private static String formatV2InvDate(LocalDate date) {
        return date.format(V2_INV_DATE_FORMAT);
    }

    private static String mapPaymentMethodName(String paymentMethod) {
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
