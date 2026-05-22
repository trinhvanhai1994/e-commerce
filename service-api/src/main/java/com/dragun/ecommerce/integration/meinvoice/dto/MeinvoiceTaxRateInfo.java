package com.dragun.ecommerce.integration.meinvoice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * V2 publish VAT summary ({@code TaxRateInfo} in MISA doc §14.5) — required for GTGT invoices.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeinvoiceTaxRateInfo {

    @JsonProperty("VATRateName")
    private String vatRateName;

    @JsonProperty("AmountWithoutVATOC")
    private BigDecimal amountWithoutVatOC;

    @JsonProperty("VATAmountOC")
    private BigDecimal vatAmountOC;
}
