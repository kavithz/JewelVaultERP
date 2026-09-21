package com.jewelvaulterp.warehouse.repository;

import com.jewelvaulterp.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {

    Optional<Warehouse> findByBranchIdAndCode(UUID branchId, String code);

    long countByActiveTrue();

    long countByActiveFalse();
}