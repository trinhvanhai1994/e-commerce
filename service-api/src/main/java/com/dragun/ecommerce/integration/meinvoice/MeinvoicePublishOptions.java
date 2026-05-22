package com.dragun.ecommerce.integration.meinvoice;

import com.dragun.ecommerce.integration.meinvoice.config.MeinvoiceIntegrationConfig;
/**
 * Resolves V2 publish flags from {@code meinvoice.defaults.inv-series} and config overrides.
 */
public final class MeinvoicePublishOptions {

    private MeinvoicePublishOptions() {
    }

    public static boolean invoiceCalculatingMachine(MeinvoiceIntegrationConfig config) {
        Boolean override = config.getDefaults().getInvoiceCalculatingMachine();
        if (override != null) {
            return override;
        }
        return MeinvoiceInvSeriesHelper.isCalculatingMachineSeries(config.getDefaults().getInvSeries());
    }

    public static int signType(MeinvoiceIntegrationConfig config) {
        if (invoiceCalculatingMachine(config)) {
            return config.getPublish().getSignTypeMtt();
        }
        return config.getPublish().getSignType();
    }
}
