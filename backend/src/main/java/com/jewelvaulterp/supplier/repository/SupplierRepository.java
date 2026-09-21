package com.jewelvaulterp.supplier.repository;

import com.jewelvaulterp.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    Optional<Supplier> findByCompanyIdAndCode(
            UUID companyId,
            String code
    );

    long countByActiveTrue();

    long countByActiveFalse();
}