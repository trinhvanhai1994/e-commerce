package com.dragun.ecommerce.service;

import com.dragun.ecommerce.model.dto.request.ChangePasswordRequest;
import com.dragun.ecommerce.model.dto.request.CustomerLoginRequest;
import com.dragun.ecommerce.model.dto.request.CustomerRegisterRequest;
import com.dragun.ecommerce.model.dto.response.CustomerAuthResponse;
import com.dragun.ecommerce.model.dto.response.CustomerProfileResponse;
import com.dragun.ecommerce.model.entity.Customer;
import com.dragun.ecommerce.model.entity.CustomerRefreshToken;
import com.dragun.ecommerce.repository.CustomerRefreshTokenRepository;
import com.dragun.ecommerce.repository.CustomerRepository;
import com.dragun.ecommerce.security.JwtTokenProvider;
import com.dragun.ecommerce.util.PhoneNumbers;
import com.dragun.ecommerce.util.SlidingWindowLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Storefront customer sessions (task-003 phase 1). Login name is the normalized phone number; no OTP in this
 * version (R6), so the phone is not verified. Access token: short JWT (typ=customer). Refresh token: random,
 * stored hashed, rotated on every refresh, revoked on logout and password change.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerAuthService {

    /** Version of Terms + Privacy the customer accepted at sign-up. Bump when those pages change. */
    public static final String CONSENT_VERSION = "2026-11-18";
    static final int MAX_FAILED_LOGINS = 5;
    static final long LOCK_MINUTES = 15;
    static final int PASSWORD_MIN = 8;
    static final int PASSWORD_MAX = 64;
    static final String INVALID_LOGIN = "Số điện thoại hoặc mật khẩu không đúng";

    private final CustomerRepository customerRepository;
    private final CustomerRefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${customer.auth.refresh-token-days:30}")
    private long refreshTokenDays;

    private final SecureRandom random = new SecureRandom();
    // Per IP: sign-ups are rare for a real person; logins allow typos but stop password spraying
    private final SlidingWindowLimiter registerLimiter = new SlidingWindowLimiter(5, 60 * 60 * 1000L);
    private final SlidingWindowLimiter loginLimiter = new SlidingWindowLimiter(20, 10 * 60 * 1000L);

    @Transactional
    public CustomerAuthResponse register(CustomerRegisterRequest request, String clientKey, String userAgent) {
        if (request.getWebsite() != null && !request.getWebsite().isBlank()) {
            log.info("Register honeypot hit client={}", clientKey);
            throw badRequest("Không thể đăng ký lúc này. Vui lòng thử lại sau.");
        }
        String phone = requirePhone(request.getPhone());
        String fullName = cleanText(request.getFullName());
        if (fullName == null) {
            throw badRequest("Vui lòng nhập họ tên");
        }
        if (!request.isConsent()) {
            throw badRequest("Vui lòng đồng ý Điều khoản sử dụng và Chính sách bảo mật");
        }
        validatePassword(request.getPassword(), phone);
        if (!registerLimiter.tryAcquire(clientKey)) {
            throw tooMany();
        }
        if (customerRepository.existsByPhone(phone)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Số điện thoại đã được đăng ký. Hãy đăng nhập, hoặc gọi hotline nếu bạn quên mật khẩu.");
        }
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        Customer customer = customerRepository.saveAndFlush(Customer.builder()
            .phone(phone)
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .fullName(fullName)
            .email(cleanText(request.getEmail()))
            .status(Customer.STATUS_ACTIVE)
            .consentAt(now)
            .consentVersion(CONSENT_VERSION)
            .lastLoginAt(now)
            .build());
        log.info("Customer {} registered", customer.getId());
        return issueSession(customer, userAgent);
    }

    /**
     * Same message for unknown phone and wrong password, so the form does not reveal who has an account.
     * After MAX_FAILED_LOGINS wrong passwords the account is locked for LOCK_MINUTES.
     */
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public CustomerAuthResponse login(CustomerLoginRequest request, String clientKey, String userAgent) {
        if (!loginLimiter.tryAcquire(clientKey)) {
            throw tooMany();
        }
        String phone = PhoneNumbers.normalize(request.getPhone());
        Customer customer = phone == null ? null : customerRepository.findByPhone(phone).orElse(null);
        if (customer == null || Customer.STATUS_DELETED.equals(customer.getStatus())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_LOGIN);
        }
        if (Customer.STATUS_LOCKED.equals(customer.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Tài khoản đã bị khoá. Vui lòng gọi hotline để được hỗ trợ.");
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (customer.getLockedUntil() != null && customer.getLockedUntil().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Bạn đã nhập sai mật khẩu nhiều lần. Vui lòng thử lại sau " + LOCK_MINUTES + " phút.");
        }
        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            int failed = customer.getFailedLoginCount() + 1;
            if (failed >= MAX_FAILED_LOGINS) {
                customer.setFailedLoginCount(0);
                customer.setLockedUntil(now.plusMinutes(LOCK_MINUTES));
            } else {
                customer.setFailedLoginCount(failed);
            }
            customerRepository.save(customer);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_LOGIN);
        }
        customer.setFailedLoginCount(0);
        customer.setLockedUntil(null);
        customer.setLastLoginAt(now.truncatedTo(ChronoUnit.MICROS));
        return issueSession(customerRepository.saveAndFlush(customer), userAgent);
    }

    /** Rotates the refresh token: the old one stops working, a new pair is returned. */
    @Transactional
    public CustomerAuthResponse refresh(String refreshToken, String userAgent) {
        CustomerRefreshToken stored = refreshTokenRepository.findByTokenHash(hash(refreshToken))
            .orElseThrow(this::sessionExpired);
        OffsetDateTime now = OffsetDateTime.now();
        if (!stored.isActive(now)) {
            throw sessionExpired();
        }
        Customer customer = customerRepository.findById(stored.getCustomerId()).orElseThrow(this::sessionExpired);
        if (!Customer.STATUS_ACTIVE.equals(customer.getStatus())) {
            throw sessionExpired();
        }
        stored.setRevokedAt(now);
        refreshTokenRepository.save(stored);
        return issueSession(customer, userAgent);
    }

    /** Idempotent: an unknown or already revoked token is not an error. */
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hash(refreshToken)).ifPresent(t -> {
            if (t.getRevokedAt() == null) {
                t.setRevokedAt(OffsetDateTime.now());
                refreshTokenRepository.save(t);
            }
        });
    }

    /**
     * Ends every session of the account (other devices included) and returns a new one for this device.
     */
    @Transactional
    public CustomerAuthResponse changePassword(Long customerId, ChangePasswordRequest request, String userAgent) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), customer.getPasswordHash())) {
            throw badRequest("Mật khẩu hiện tại không đúng");
        }
        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw badRequest("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        validatePassword(request.getNewPassword(), customer.getPhone());
        customer.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        customer.setMustChangePassword(false);
        customer.setTokenVersion(customer.getTokenVersion() + 1);
        refreshTokenRepository.revokeAllForCustomer(customerId, OffsetDateTime.now());
        log.info("Customer {} changed password", customerId);
        return issueSession(customerRepository.saveAndFlush(customer), userAgent);
    }

    // ---------- helpers ----------

    private CustomerAuthResponse issueSession(Customer customer, String userAgent) {
        String refreshToken = newRefreshToken();
        refreshTokenRepository.save(CustomerRefreshToken.builder()
            .customerId(customer.getId())
            .tokenHash(hash(refreshToken))
            .expiresAt(OffsetDateTime.now().plusDays(refreshTokenDays))
            .userAgent(truncate(userAgent, 255))
            .build());
        return CustomerAuthResponse.builder()
            .accessToken(jwtTokenProvider.generateCustomerToken(customer.getId(), customer.getTokenVersion()))
            .expiresIn(jwtTokenProvider.getCustomerAccessTokenSeconds())
            .refreshToken(refreshToken)
            .customer(CustomerProfileResponse.from(customer))
            .build();
    }

    static String requirePhone(String raw) {
        String phone = PhoneNumbers.normalize(raw);
        if (phone == null) {
            throw badRequest("Số điện thoại không hợp lệ (ví dụ 0912345678)");
        }
        return phone;
    }

    /** 8 to 64 characters, at least one letter and one digit, not the phone number itself. */
    static void validatePassword(String password, String phone) {
        if (password == null || password.length() < PASSWORD_MIN || password.length() > PASSWORD_MAX) {
            throw badRequest("Mật khẩu từ " + PASSWORD_MIN + " đến " + PASSWORD_MAX + " ký tự");
        }
        if (!password.matches(".*\\p{L}.*") || !password.matches(".*\\d.*")) {
            throw badRequest("Mật khẩu phải có cả chữ và số");
        }
        if (password.contains(phone)) {
            throw badRequest("Mật khẩu không được chứa số điện thoại");
        }
    }

    private String newRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    static String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replaceAll("\\p{Cntrl}", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }

    private ResponseStatusException sessionExpired() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
    }

    private static ResponseStatusException tooMany() {
        return new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
            "Bạn thao tác quá nhiều lần. Vui lòng thử lại sau ít phút.");
    }

    static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
