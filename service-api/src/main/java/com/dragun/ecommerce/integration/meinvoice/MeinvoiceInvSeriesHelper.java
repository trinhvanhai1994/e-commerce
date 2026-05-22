package com.dragun.ecommerce.integration.meinvoice;

import org.springframework.util.StringUtils;

/**
 * Parses MISA {@code InvSeries} per doc (char 5: T = thường, M = máy tính tiền).
 */
public final class MeinvoiceInvSeriesHelper {

    /** 1-based position in MISA docs; 0-based index {@value}. */
    private static final int CALCULATING_MACHINE_CHAR_INDEX = 4;

    private MeinvoiceInvSeriesHelper() {
    }

    public static boolean isCalculatingMachineSeries(String invSeries) {
        if (!StringUtils.hasText(invSeries) || invSeries.length() <= CALCULATING_MACHINE_CHAR_INDEX) {
            return false;
        }
        char form = invSeries.charAt(CALCULATING_MACHINE_CHAR_INDEX);
        return form == 'M' || form == 'm';
    }
}
