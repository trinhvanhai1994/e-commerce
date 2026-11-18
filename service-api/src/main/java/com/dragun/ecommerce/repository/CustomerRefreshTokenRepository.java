package com.dragun.ecommerce.repository;

import com.dragun.ecommerce.model.entity.CustomerRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface CustomerRefreshTokenRepository extends JpaRepository<CustomerRefreshToken, Long> {

    Optional<CustomerRefreshToken> findByTokenHash(String tokenHash);

    /** Ends every session of the account (password change, lock). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CustomerRefreshToken t SET t.revokedAt = :now WHERE t.customerId = :customerId AND t.revokedAt IS NULL")
    int revokeAllForCustomer(@Param("customerId") Long customerId, @Param("now") OffsetDateTime now);
}
