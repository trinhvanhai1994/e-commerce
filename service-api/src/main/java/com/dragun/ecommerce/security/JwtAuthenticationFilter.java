package com.dragun.ecommerce.security;

import com.dragun.ecommerce.model.entity.Customer;
import com.dragun.ecommerce.repository.CustomerRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads "Authorization: Bearer" and authenticates either an admin (users table) or a storefront customer
 * (customers table, token claim typ=customer). A request without a token, or with an invalid one, continues
 * anonymous; the security rules then decide.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final CustomerRepository customerRepository;

    /**
     * Paths that never need an identity. /api/dragun/products/** and /api/extend/orders are NOT skipped: writing a
     * review needs the customer, and an order placed while logged in is attached to the account.
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/public/") ||
               path.startsWith("/api/dragun/content/") ||
               path.startsWith("/provinces") ||
               path.startsWith("/districts/") ||
               path.startsWith("/wards/") ||
               path.startsWith("/images/") ||
               path.equals("/api/dragun/admin/login");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        try {
            Claims claims = jwtTokenProvider.parseClaims(jwt);
            if (JwtTokenProvider.isCustomerToken(claims)) {
                authenticateCustomer(claims, request);
            } else {
                authenticateAdmin(jwt, claims.getSubject(), request);
            }
        } catch (Exception e) {
            // Token is invalid or expired: continue without authentication
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateAdmin(String jwt, String username, HttpServletRequest request) {
        if (username == null) {
            return;
        }
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        if (jwtTokenProvider.validateToken(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    /** The account must still be active and the token not older than the last password change or lock. */
    private void authenticateCustomer(Claims claims, HttpServletRequest request) {
        Long customerId = JwtTokenProvider.customerId(claims);
        Integer version = claims.get(JwtTokenProvider.CLAIM_TOKEN_VERSION, Integer.class);
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null || !Customer.STATUS_ACTIVE.equals(customer.getStatus())
                || version == null || version != customer.getTokenVersion()) {
            return;
        }
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            new CustomerPrincipal(customer.getId(), customer.getPhone()),
            null,
            List.of(new SimpleGrantedAuthority(CustomerPrincipal.ROLE))
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
