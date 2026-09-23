package com.jewelvaulterp.manufacturing.repository;

import com.jewelvaulterp.manufacturing.entity.ManufacturingOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ManufacturingOrderRepository
        extends JpaRepository<ManufacturingOrder, UUID> {

    Optional<ManufacturingOrder> findByCompanyIdAndOrderNumber(
            UUID companyId,
            String orderNumber
    );
}