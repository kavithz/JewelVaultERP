package com.jewelvaulterp.stockmovement.repository;

import com.jewelvaulterp.stockmovement.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository
        extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findByInventoryIdOrderByMovementDateDesc(
            UUID inventoryId
    );
}