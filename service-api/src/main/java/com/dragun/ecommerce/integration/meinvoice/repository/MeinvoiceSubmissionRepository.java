package com.dragun.ecommerce.integration.meinvoice.repository;

import com.dragun.ecommerce.integration.meinvoice.model.MeinvoiceSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeinvoiceSubmissionRepository extends JpaRepository<MeinvoiceSubmission, Long> {

    Optional<MeinvoiceSubmission> findByRefId(String refId);

    Optional<MeinvoiceSubmission> findFirstByOrderBusinessIdAndSuccessTrueOrderByCreatedAtDesc(String orderBusinessId);

    Optional<MeinvoiceSubmission> findFirstByOrderBusinessIdOrderByCreatedAtDesc(String orderBusinessId);

    List<MeinvoiceSubmission> findByOrderBusinessIdOrderByCreatedAtDesc(String orderBusinessId);

    List<MeinvoiceSubmission> findByOrderBusinessIdInAndSuccessTrueOrderByCreatedAtDesc(
            Collection<String> orderBusinessIds);
}
