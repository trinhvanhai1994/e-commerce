package com.dragun.ecommerce.model.dto.response;

import com.dragun.ecommerce.model.entity.Customer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class CustomerProfileResponse {
    private Long id;
    private String phone;
    private String fullName;
    private String email;
    private LocalDate birthDate;
    private String gender;
    private String addressRecipient;
    private String addressPhone;
    private String addressLine;
    private String provinceCode;
    private String districtCode;
    private String wardCode;
    private boolean mustChangePassword;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static CustomerProfileResponse from(Customer c) {
        return CustomerProfileResponse.builder()
            .id(c.getId())
            .phone(c.getPhone())
            .fullName(c.getFullName())
            .email(c.getEmail())
            .birthDate(c.getBirthDate())
            .gender(c.getGender())
            .addressRecipient(c.getAddressRecipient())
            .addressPhone(c.getAddressPhone())
            .addressLine(c.getAddressLine())
            .provinceCode(c.getProvinceCode())
            .districtCode(c.getDistrictCode())
            .wardCode(c.getWardCode())
            .mustChangePassword(c.isMustChangePassword())
            .createdAt(c.getCreatedAt())
            .updatedAt(c.getUpdatedAt())
            .build();
    }
}
