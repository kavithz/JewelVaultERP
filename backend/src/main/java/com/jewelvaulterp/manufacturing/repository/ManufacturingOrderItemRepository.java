package com.jewelvaulterp.manufacturing.repository;

import com.jewelvaulterp.manufacturing.entity.ManufacturingOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ManufacturingOrderItemRepository
        extends JpaRepository<ManufacturingOrderItem, UUID> {

    List<ManufacturingOrderItem> findByManufacturingOrderId(
            UUID manufacturingOrderId
    );
}