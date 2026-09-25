package com.jewelvaulterp.purchase.repository;

import com.jewelvaulterp.purchase.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    List<Purchase> findByCompanyId(UUID companyId);

    Optional<Purchase> findByCompanyIdAndPurchaseNumber(
            UUID companyId,
            String purchaseNumber
    );
}