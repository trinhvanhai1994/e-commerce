package com.dragun.ecommerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSigningKey())
            .compact();
    }
    
    // ---------- customer access tokens ----------
    // Distinguished from admin tokens by the "typ" claim; the subject is never an admin username.

    public static final String CLAIM_TYPE = "typ";
    public static final String TYPE_CUSTOMER = "customer";
    public static final String CLAIM_TOKEN_VERSION = "ver";
    private static final String CUSTOMER_SUBJECT_PREFIX = "customer:";

    @Value("${customer.auth.access-token-minutes:60}")
    private long customerAccessTokenMinutes;

    public long getCustomerAccessTokenSeconds() {
        return customerAccessTokenMinutes * 60;
    }

    public String generateCustomerToken(Long customerId, int tokenVersion) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .claims(Map.of(CLAIM_TYPE, TYPE_CUSTOMER, CLAIM_TOKEN_VERSION, tokenVersion))
            .subject(CUSTOMER_SUBJECT_PREFIX + customerId)
            .issuedAt(new Date(now))
            .expiration(new Date(now + customerAccessTokenMinutes * 60_000))
            .signWith(getSigningKey())
            .compact();
    }

    /** Verifies signature and expiry; throws on an invalid token. */
    public Claims parseClaims(String token) {
        return extractAllClaims(token);
    }

    public static boolean isCustomerToken(Claims claims) {
        return TYPE_CUSTOMER.equals(claims.get(CLAIM_TYPE, String.class))
            && claims.getSubject() != null && claims.getSubject().startsWith(CUSTOMER_SUBJECT_PREFIX);
    }

    public static Long customerId(Claims claims) {
        return Long.valueOf(claims.getSubject().substring(CUSTOMER_SUBJECT_PREFIX.length()));
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}


