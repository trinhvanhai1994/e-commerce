package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.dto.request.CustomerProfileRequest;
import com.dragun.ecommerce.model.dto.response.CustomerProfileResponse;
import com.dragun.ecommerce.model.entity.Customer;
import com.dragun.ecommerce.repository.CustomerRepository;
import com.dragun.ecommerce.repository.ProductReviewRepository;
import com.dragun.ecommerce.util.PhoneNumbers;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/** "Tài khoản" page: profile and default shipping address of the logged-in customer. */
@Service
@RequiredArgsConstructor
public class CustomerAccountService {

    private static final Set<String> GENDERS = Set.of("MALE", "FEMALE", "OTHER");

    private final CustomerRepository customerRepository;
    private final ProductReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(Long customerId) {
        return CustomerProfileResponse.from(requireCustomer(customerId));
    }

    /** Optimistic lock on updatedAt (409 when another tab or device saved first). */
    @Transactional
    public CustomerProfileResponse updateProfile(Long customerId, CustomerProfileRequest request) {
        Customer customer = requireCustomer(customerId);
        if (customer.getUpdatedAt() == null
                || !customer.getUpdatedAt().toInstant().equals(request.getUpdatedAt().toInstant())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Thông tin tài khoản đã được cập nhật ở nơi khác. Hãy tải lại trang.");
        }
        String fullName = CustomerAuthService.cleanText(request.getFullName());
        if (fullName == null) {
            throw CustomerAuthService.badRequest("Vui lòng nhập họ tên");
        }
        String gender = blankToNull(request.getGender());
        if (gender != null) {
            gender = gender.toUpperCase(Locale.ROOT);
            if (!GENDERS.contains(gender)) {
                throw CustomerAuthService.badRequest("Giới tính không hợp lệ");
            }
        }
        String addressPhone = blankToNull(request.getAddressPhone());
        if (addressPhone != null) {
            addressPhone = PhoneNumbers.normalize(addressPhone);
            if (addressPhone == null) {
                throw CustomerAuthService.badRequest("Số điện thoại nhận hàng không hợp lệ");
            }
        }
        boolean nameChanged = !Objects.equals(customer.getFullName(), fullName);

        customer.setFullName(fullName);
        customer.setEmail(CustomerAuthService.cleanText(request.getEmail()));
        customer.setBirthDate(request.getBirthDate());
        customer.setGender(gender);
        customer.setAddressRecipient(CustomerAuthService.cleanText(request.getAddressRecipient()));
        customer.setAddressPhone(addressPhone);
        customer.setAddressLine(CustomerAuthService.cleanText(request.getAddressLine()));
        customer.setProvinceCode(blankToNull(request.getProvinceCode()));
        customer.setDistrictCode(blankToNull(request.getDistrictCode()));
        customer.setWardCode(blankToNull(request.getWardCode()));
        // saveAndFlush so @PreUpdate sets updatedAt before the response (next save's lock token) is built
        Customer saved = customerRepository.saveAndFlush(customer);
        if (nameChanged) {
            // Reviews show the account's current name (task-003 Q-AUTH-022)
            reviewRepository.updateAuthorNameByCustomerId(customerId, fullName);
        }
        return CustomerProfileResponse.from(saved);
    }

    private Customer requireCustomer(Long customerId) {
        return customerRepository.findById(customerId)
            .filter(c -> !Customer.STATUS_DELETED.equals(c.getStatus()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại"));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
