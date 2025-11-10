package com.dragun.ecommerce.integration.pancake.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PancakeApiResponse<T> {
    private Boolean success;
    private T data;
    private String message;
    private Integer code;
}

