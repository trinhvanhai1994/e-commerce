package com.dragun.ecommerce.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Profile update. Phone (login name) cannot be changed here. */
@Data
public class CustomerProfileRequest {

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email tối đa 255 ký tự")
    private String email;

    @Past(message = "Ngày sinh không hợp lệ")
    private LocalDate birthDate;

    /** MALE / FEMALE / OTHER or empty. */
    private String gender;

    @Size(max = 100, message = "Tên người nhận tối đa 100 ký tự")
    private String addressRecipient;

    private String addressPhone;

    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String addressLine;

    private String provinceCode;
    private String districtCode;
    private String wardCode;

    /** Optimistic lock token from the last read. */
    @NotNull(message = "Thiếu updatedAt. Hãy tải lại trang rồi lưu lại.")
    private OffsetDateTime updatedAt;
}
