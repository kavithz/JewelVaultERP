package com.jewelvaulterp.purchase.repository;

import com.jewelvaulterp.purchase.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    Page<Purchase> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Purchase> findByCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    List<Purchase> findByCompanyId(UUID companyId);

    Optional<Purchase> findByCompanyIdAndPurchaseNumber(
            UUID companyId,
            String purchaseNumber
    );
}