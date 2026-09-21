package com.jewelvaulterp.inventory.repository;

import com.jewelvaulterp.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByWarehouseIdAndProductId(
            UUID warehouseId,
            UUID productId
    );
}