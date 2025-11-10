package com.dragun.ecommerce.integration.pancake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PancakeCustomerDto {
    private String id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String province;
    private String district;
    private String ward;
    private String createdAt;
    private String updatedAt;
}

