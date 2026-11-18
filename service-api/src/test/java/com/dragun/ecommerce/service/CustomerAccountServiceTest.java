package com.dragun.ecommerce.service;

import com.dragun.ecommerce.TestCustomers;
import com.dragun.ecommerce.TestcontainersConfiguration;
import com.dragun.ecommerce.model.dto.request.CustomerProfileRequest;
import com.dragun.ecommerce.model.dto.response.CustomerProfileResponse;
import com.dragun.ecommerce.model.dto.response.ProductReviewResponse;
import com.dragun.ecommerce.model.entity.Customer;
import com.dragun.ecommerce.repository.CustomerRepository;
import com.dragun.ecommerce.repository.ProductRepository;
import com.dragun.ecommerce.repository.ProductReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("CustomerAccountService")
class CustomerAccountServiceTest {

    @Autowired
    private CustomerAccountService accountService;

    @Autowired
    private ProductReviewService reviewService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private EntityManager entityManager;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = TestCustomers.create(customerRepository, "Lê Văn Cường");
    }

    private CustomerProfileRequest edit(CustomerProfileResponse current) {
        CustomerProfileRequest r = new CustomerProfileRequest();
        r.setFullName(current.getFullName());
        r.setUpdatedAt(current.getUpdatedAt());
        return r;
    }

    @Test
    @DisplayName("saves profile and default address; a stale updatedAt gets 409")
    void update_optimisticLock() {
        CustomerProfileResponse loaded = accountService.getProfile(customer.getId());
        CustomerProfileRequest r = edit(loaded);
        r.setEmail("cuong@example.com");
        r.setGender("male");
        r.setAddressRecipient("Lê Văn Cường");
        r.setAddressPhone("+84 912 000 111");
        r.setAddressLine("12 Hàng Bài");
        r.setProvinceCode("01");

        CustomerProfileResponse saved = accountService.updateProfile(customer.getId(), r);

        assertThat(saved.getEmail()).isEqualTo("cuong@example.com");
        assertThat(saved.getGender()).isEqualTo("MALE");
        assertThat(saved.getAddressPhone()).isEqualTo("0912000111");
        assertThat(saved.getPhone()).isEqualTo(customer.getPhone());

        assertStatus(() -> accountService.updateProfile(customer.getId(), r), HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("invalid gender or delivery phone is rejected with 400")
    void update_validation() {
        CustomerProfileResponse loaded = accountService.getProfile(customer.getId());
        CustomerProfileRequest badGender = edit(loaded);
        badGender.setGender("ROBOT");
        CustomerProfileRequest badPhone = edit(loaded);
        badPhone.setAddressPhone("123");

        assertStatus(() -> accountService.updateProfile(customer.getId(), badGender), HttpStatus.BAD_REQUEST);
        assertStatus(() -> accountService.updateProfile(customer.getId(), badPhone), HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("renaming the account renames the author of its reviews")
    void rename_updatesReviews() {
        Long productId = productRepository.findAll().get(0).getId();
        ProductReviewResponse review = reviewService.submit(productId, customer.getId(), 5, "Ngon và tiện",
            null, List.of(), "test-" + UUID.randomUUID());
        CustomerProfileRequest r = edit(accountService.getProfile(customer.getId()));
        r.setFullName("Lê Cường");

        accountService.updateProfile(customer.getId(), r);
        entityManager.clear();

        assertThat(reviewRepository.findById(review.getId()).orElseThrow().getAuthorName()).isEqualTo("Lê Cường");
    }

    private static void assertStatus(Runnable call, HttpStatus expected) {
        assertThatThrownBy(call::run)
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(expected);
    }
}
