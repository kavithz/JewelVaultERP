package com.jewelvaulterp.supplier.repository;

import com.jewelvaulterp.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    Page<Supplier> findByCompanyId(UUID companyId, Pageable pageable);

    Page<Supplier> findByCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    Optional<Supplier> findByCompanyIdAndCode(
            UUID companyId,
            String code
    );

    long countByActiveTrue();

    long countByActiveFalse();
}