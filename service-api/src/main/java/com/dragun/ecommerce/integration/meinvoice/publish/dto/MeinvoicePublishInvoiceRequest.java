package com.dragun.ecommerce.integration.meinvoice.publish.dto;

import com.dragun.ecommerce.integration.meinvoice.publish.MeinvoicePublishConstants;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeinvoicePublishInvoiceRequest {

    @JsonProperty(MeinvoicePublishConstants.JSON_FIELD_SIGN_TYPE)
    private Integer signType;

    @JsonProperty(MeinvoicePublishConstants.JSON_FIELD_CERTIFICATE_SN)
    private String certificateSn;

    @JsonProperty(MeinvoicePublishConstants.JSON_FIELD_INVOICE_DATA)
    private List<MeinvoiceV2InvoiceData> invoiceData;

    @JsonProperty(MeinvoicePublishConstants.JSON_FIELD_PUBLISH_INVOICE_DATA)
    private Object publishInvoiceData;
}
