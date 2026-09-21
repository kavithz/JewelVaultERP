package com.jewelvaulterp.inventory.service;

import com.jewelvaulterp.inventory.dto.CreateInventoryRequest;
import com.jewelvaulterp.inventory.dto.InventoryResponse;
import com.jewelvaulterp.inventory.dto.UpdateInventoryRequest;
import com.jewelvaulterp.inventory.entity.Inventory;
import com.jewelvaulterp.inventory.repository.InventoryRepository;
import com.jewelvaulterp.product.entity.Product;
import com.jewelvaulterp.product.repository.ProductRepository;
import com.jewelvaulterp.warehouse.entity.Warehouse;
import com.jewelvaulterp.warehouse.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            WarehouseRepository warehouseRepository,
            ProductRepository productRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
    }

    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public InventoryResponse getInventory(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));

        return toResponse(inventory);
    }

    public InventoryResponse createInventory(
            CreateInventoryRequest request
    ) {
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        validateQuantities(
                request.quantity(),
                request.reservedQuantity()
        );

        if (inventoryRepository.findByWarehouseIdAndProductId(
                request.warehouseId(),
                request.productId()
        ).isPresent()) {
            throw new IllegalArgumentException(
                    "Inventory already exists for this warehouse and product"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        Inventory inventory = new Inventory(
                UUID.randomUUID(),
                warehouse,
                product,
                request.quantity(),
                request.reservedQuantity(),
                now,
                now
        );

        return toResponse(inventoryRepository.save(inventory));
    }

    public InventoryResponse updateInventory(
            UUID id,
            UpdateInventoryRequest request
    ) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found"));

        validateQuantities(
                request.quantity(),
                request.reservedQuantity()
        );

        inventory.update(
                request.quantity(),
                request.reservedQuantity()
        );

        return toResponse(inventoryRepository.save(inventory));
    }

    private void validateQuantities(
            java.math.BigDecimal quantity,
            java.math.BigDecimal reservedQuantity
    ) {
        if (reservedQuantity.compareTo(quantity) > 0) {
            throw new IllegalArgumentException(
                    "Reserved quantity cannot be greater than quantity"
            );
        }
    }

    private InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getWarehouse().getId(),
                inventory.getProduct().getId(),
                inventory.getQuantity(),
                inventory.getReservedQuantity(),
                inventory.getAvailableQuantity(),
                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}