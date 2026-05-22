package com.dragun.ecommerce.integration.meinvoice;

import com.dragun.ecommerce.integration.meinvoice.publish.MeinvoicePublishConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Shared VAT / rounding rules for V1 ({@code /webapp/insert}) and V2 ({@code POST /invoice}) payloads.
 */
public final class MeinvoiceVatMath {

    private MeinvoiceVatMath() {
    }

    public static void validateVatRatePercent(int vatRatePercent) {
        if (vatRatePercent < -3 || vatRatePercent > 100) {
            throw new IllegalStateException(
                    "meinvoice.defaults.default-vat-rate must be between -3 and 100 (MeInvoice line VATRate semantics).");
        }
    }

    /**
     * Converts a VAT-inclusive amount to exclusive VAT: net = gross * 100 / (100 + R).
     */
    public static BigDecimal unitPriceExcludingVat(BigDecimal grossAmount, int vatRatePercent) {
        if (vatRatePercent <= 0) {
            return grossAmount.setScale(4, RoundingMode.HALF_UP);
        }
        BigDecimal hundred = BigDecimal.valueOf(100);
        BigDecimal divisor = hundred.add(BigDecimal.valueOf(vatRatePercent));
        return grossAmount.multiply(hundred).divide(divisor, 4, RoundingMode.HALF_UP);
    }

    public static BigDecimal computeVatAmount(BigDecimal amountBeforeVat, int vatRatePercent) {
        if (vatRatePercent < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amountBeforeVat
                .multiply(BigDecimal.valueOf(vatRatePercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal scale2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    /** MISA {@code VATRateName} on V2 lines / {@code TaxRateInfo}. */
    public static String formatVatRateName(BigDecimal vatRate) {
        if (vatRate == null) {
            return null;
        }
        BigDecimal normalized = vatRate.stripTrailingZeros();
        if (normalized.scale() <= 0 || normalized.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            return String.format(Locale.ROOT, MeinvoicePublishConstants.VAT_RATE_NAME_FORMAT_PERCENT, normalized.intValue());
        }
        return String.format(Locale.ROOT, MeinvoicePublishConstants.VAT_RATE_NAME_FORMAT_OTHER, normalized.toPlainString());
    }
}
