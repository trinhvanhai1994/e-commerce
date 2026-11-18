package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerLoginRequest {

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    private String phone;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    private String password;
}
