package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Storefront sign-up. Phone format and password policy are checked in CustomerAuthService. */
@Data
public class CustomerRegisterRequest {

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    private String phone;

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu")
    private String password;

    @AssertTrue(message = "Vui lòng đồng ý Điều khoản sử dụng và Chính sách bảo mật")
    private boolean consent;

    /** Honeypot: hidden in the form, real users leave it empty. */
    private String website;
}
