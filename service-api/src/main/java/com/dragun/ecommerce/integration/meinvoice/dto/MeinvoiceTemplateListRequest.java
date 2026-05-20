package com.dragun.ecommerce.integration.meinvoice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Body for {@code POST .../templates} per official Postman collection (PascalCase fields).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeinvoiceTemplateListRequest {

    @JsonProperty("TaxCode")
    private String taxCode;

    @JsonProperty("UserName")
    private String userName;

    @JsonProperty("Password")
    private String password;
}
