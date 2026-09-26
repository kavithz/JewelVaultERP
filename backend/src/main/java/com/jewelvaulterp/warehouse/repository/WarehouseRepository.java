package com.jewelvaulterp.warehouse.repository;

import com.jewelvaulterp.warehouse.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    Page<Warehouse> findByBranchCompanyId(UUID companyId, Pageable pageable);

    Page<Warehouse> findByBranchCompanyIdAndUpdatedAtAfter(UUID companyId, LocalDateTime updatedSince, Pageable pageable);

    Optional<Warehouse> findByBranchIdAndCode(UUID branchId, String code);

    long countByActiveTrue();

    long countByActiveFalse();
}