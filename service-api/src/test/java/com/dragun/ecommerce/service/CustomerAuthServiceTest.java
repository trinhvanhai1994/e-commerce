package com.dragun.ecommerce.service;

import com.dragun.ecommerce.TestCustomers;
import com.dragun.ecommerce.TestcontainersConfiguration;
import com.dragun.ecommerce.model.dto.request.ChangePasswordRequest;
import com.dragun.ecommerce.model.dto.request.CustomerLoginRequest;
import com.dragun.ecommerce.model.dto.request.CustomerRegisterRequest;
import com.dragun.ecommerce.model.dto.response.CustomerAuthResponse;
import com.dragun.ecommerce.model.entity.Customer;
import com.dragun.ecommerce.repository.CustomerRepository;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Customer accounts on a real PostgreSQL (V22). Each test rolls back; rate limiters are in-memory and shared,
 * so every test uses its own client key.
 */
@SpringBootTest
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
@Transactional
@DisplayName("CustomerAuthService")
class CustomerAuthServiceTest {

    private static final String PASSWORD = "matkhau123";

    @Autowired
    private CustomerAuthService authService;

    @Autowired
    private CustomerRepository customerRepository;

    private String clientKey;
    private String phone;

    @BeforeEach
    void setUp() {
        clientKey = "test-" + UUID.randomUUID();
        phone = TestCustomers.randomPhone();
    }

    private CustomerRegisterRequest registerRequest(String rawPhone, String password) {
        CustomerRegisterRequest r = new CustomerRegisterRequest();
        r.setPhone(rawPhone);
        r.setFullName("  Nguyễn Văn An ");
        r.setEmail("");
        r.setPassword(password);
        r.setConsent(true);
        return r;
    }

    private CustomerAuthResponse register() {
        return authService.register(registerRequest(phone, PASSWORD), clientKey, "junit");
    }

    private CustomerAuthResponse login(String password) {
        CustomerLoginRequest r = new CustomerLoginRequest();
        r.setPhone(phone);
        r.setPassword(password);
        return authService.login(r, clientKey, "junit");
    }

    @Test
    @DisplayName("register normalizes the phone, stores consent and returns a session")
    void register_normalizesAndLogsIn() {
        String intl = "+84 " + phone.substring(1, 4) + " " + phone.substring(4, 7) + " " + phone.substring(7);

        CustomerAuthResponse res = authService.register(registerRequest(intl, PASSWORD), clientKey, "junit");

        assertThat(res.getAccessToken()).isNotBlank();
        assertThat(res.getRefreshToken()).isNotBlank();
        assertThat(res.getExpiresIn()).isPositive();
        assertThat(res.getCustomer().getPhone()).isEqualTo(phone);
        assertThat(res.getCustomer().getFullName()).isEqualTo("Nguyễn Văn An");
        Customer stored = customerRepository.findByPhone(phone).orElseThrow();
        assertThat(stored.getPasswordHash()).isNotEqualTo(PASSWORD).startsWith("$2");
        assertThat(stored.getConsentAt()).isNotNull();
        assertThat(stored.getConsentVersion()).isEqualTo(CustomerAuthService.CONSENT_VERSION);
        assertThat(stored.getEmail()).isNull();
        assertThat(stored.getPhoneVerifiedAt()).isNull();
    }

    @Test
    @DisplayName("the same phone in another format cannot register twice (409)")
    void register_duplicatePhone() {
        register();
        String other = "84" + phone.substring(1);

        assertStatus(() -> authService.register(registerRequest(other, PASSWORD), clientKey, "junit"),
            HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("invalid phone, weak password and missing consent are rejected with 400")
    void register_validation() {
        assertStatus(() -> authService.register(registerRequest("12345", PASSWORD), clientKey, "j"), HttpStatus.BAD_REQUEST);
        assertStatus(() -> authService.register(registerRequest("0212345678", PASSWORD), clientKey, "j"), HttpStatus.BAD_REQUEST);
        assertStatus(() -> authService.register(registerRequest(phone, "abc12"), clientKey, "j"), HttpStatus.BAD_REQUEST);
        assertStatus(() -> authService.register(registerRequest(phone, "chiconchu"), clientKey, "j"), HttpStatus.BAD_REQUEST);
        assertStatus(() -> authService.register(registerRequest(phone, "12345678"), clientKey, "j"), HttpStatus.BAD_REQUEST);
        assertStatus(() -> authService.register(registerRequest(phone, "a" + phone), clientKey, "j"), HttpStatus.BAD_REQUEST);
        CustomerRegisterRequest noConsent = registerRequest(phone, PASSWORD);
        noConsent.setConsent(false);
        assertStatus(() -> authService.register(noConsent, clientKey, "j"), HttpStatus.BAD_REQUEST);
        assertThat(customerRepository.existsByPhone(phone)).isFalse();
    }

    @Test
    @DisplayName("the 6th sign-up from one IP within an hour gets 429")
    void register_rateLimitedPerIp() {
        for (int i = 0; i < 5; i++) {
            authService.register(registerRequest(TestCustomers.randomPhone(), PASSWORD), clientKey, "j");
        }
        assertStatus(() -> register(), HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @DisplayName("login: right password returns a session; wrong password and unknown phone give the same 401")
    void login() {
        register();

        assertThat(login(PASSWORD).getAccessToken()).isNotBlank();

        ResponseStatusException wrong = catchStatus(() -> login("saimatkhau1"));
        assertThat(wrong.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        phone = TestCustomers.randomPhone();
        ResponseStatusException unknown = catchStatus(() -> login(PASSWORD));
        assertThat(unknown.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(unknown.getReason()).isEqualTo(wrong.getReason());
    }

    @Test
    @DisplayName("5 wrong passwords lock login for 15 minutes, even with the right password (429)")
    void login_lockAfterFailures() {
        register();
        for (int i = 0; i < CustomerAuthService.MAX_FAILED_LOGINS; i++) {
            assertStatus(() -> login("saimatkhau1"), HttpStatus.UNAUTHORIZED);
        }

        assertStatus(() -> login(PASSWORD), HttpStatus.TOO_MANY_REQUESTS);
        assertThat(customerRepository.findByPhone(phone).orElseThrow().getLockedUntil()).isNotNull();
    }

    @Test
    @DisplayName("an account locked by admin cannot log in (403)")
    void login_lockedAccount() {
        register();
        Customer c = customerRepository.findByPhone(phone).orElseThrow();
        c.setStatus(Customer.STATUS_LOCKED);
        customerRepository.saveAndFlush(c);

        assertStatus(() -> login(PASSWORD), HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("refresh rotates the token: the used one stops working")
    void refresh_rotates() {
        String first = register().getRefreshToken();

        CustomerAuthResponse next = authService.refresh(first, "junit");

        assertThat(next.getRefreshToken()).isNotEqualTo(first);
        assertStatus(() -> authService.refresh(first, "junit"), HttpStatus.UNAUTHORIZED);
        assertThat(authService.refresh(next.getRefreshToken(), "junit").getAccessToken()).isNotBlank();
    }

    @Test
    @DisplayName("logout revokes the refresh token; unknown tokens are ignored")
    void logout() {
        String refresh = register().getRefreshToken();

        authService.logout(refresh);
        authService.logout("khong-ton-tai");
        authService.logout(null);

        assertStatus(() -> authService.refresh(refresh, "junit"), HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("change password: needs the current one, ends other sessions, bumps the token version")
    void changePassword() {
        CustomerAuthResponse session = register();
        Long id = session.getCustomer().getId();
        int versionBefore = customerRepository.findById(id).orElseThrow().getTokenVersion();

        ChangePasswordRequest wrongCurrent = new ChangePasswordRequest();
        wrongCurrent.setCurrentPassword("saimatkhau1");
        wrongCurrent.setNewPassword("matkhaumoi456");
        assertStatus(() -> authService.changePassword(id, wrongCurrent, "j"), HttpStatus.BAD_REQUEST);

        ChangePasswordRequest ok = new ChangePasswordRequest();
        ok.setCurrentPassword(PASSWORD);
        ok.setNewPassword("matkhaumoi456");
        CustomerAuthResponse fresh = authService.changePassword(id, ok, "j");

        assertThat(customerRepository.findById(id).orElseThrow().getTokenVersion()).isEqualTo(versionBefore + 1);
        assertStatus(() -> authService.refresh(session.getRefreshToken(), "j"), HttpStatus.UNAUTHORIZED);
        assertThat(authService.refresh(fresh.getRefreshToken(), "j").getAccessToken()).isNotBlank();
        assertStatus(() -> login(PASSWORD), HttpStatus.UNAUTHORIZED);
        assertThat(login("matkhaumoi456").getAccessToken()).isNotBlank();
    }

    private static ResponseStatusException catchStatus(Runnable call) {
        try {
            call.run();
        } catch (ResponseStatusException e) {
            return e;
        }
        throw new AssertionError("expected ResponseStatusException");
    }

    private static void assertStatus(Runnable call, HttpStatus expected) {
        assertThatThrownBy(call::run)
            .isInstanceOf(ResponseStatusException.class)
            .extracting(e -> ((ResponseStatusException) e).getStatusCode())
            .isEqualTo(expected);
    }
}
