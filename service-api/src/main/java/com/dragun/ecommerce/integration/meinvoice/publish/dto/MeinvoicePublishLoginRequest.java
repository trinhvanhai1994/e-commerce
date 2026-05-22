package com.dragun.ecommerce.integration.meinvoice.publish.dto;

import com.dragun.ecommerce.integration.meinvoice.publish.MeinvoicePublishConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeinvoicePublishLoginRequest {

    @JsonProperty(MeinvoicePublishConstants.JSON_FIELD_APP_ID)
    private String appId;

    @JsonProperty("taxcode")
    private String taxcode;

    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;
}
