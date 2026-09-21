package com.jewelvaulterp.stockmovement.service;

import com.jewelvaulterp.inventory.entity.Inventory;
import com.jewelvaulterp.inventory.repository.InventoryRepository;
import com.jewelvaulterp.stockmovement.dto.CreateStockMovementRequest;
import com.jewelvaulterp.stockmovement.dto.StockMovementResponse;
import com.jewelvaulterp.stockmovement.entity.MovementType;
import com.jewelvaulterp.stockmovement.entity.StockMovement;
import com.jewelvaulterp.stockmovement.repository.StockMovementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final InventoryRepository inventoryRepository;

    public StockMovementService(
            StockMovementRepository stockMovementRepository,
            InventoryRepository inventoryRepository
    ) {
        this.stockMovementRepository = stockMovementRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public List<StockMovementResponse> getAllMovements() {
        return stockMovementRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public StockMovementResponse getMovement(UUID id) {
        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Stock movement not found"));

        return toResponse(movement);
    }

    @Transactional
    public StockMovementResponse createMovement(
            CreateStockMovementRequest request
    ) {
        Inventory inventory = inventoryRepository.findById(request.inventoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Inventory not found"));

        BigDecimal currentQuantity = inventory.getQuantity();
        BigDecimal movementQuantity = request.quantity();

        BigDecimal newQuantity;

        if (isIncoming(request.movementType())) {
            newQuantity = currentQuantity.add(movementQuantity);
        } else {
            newQuantity = currentQuantity.subtract(movementQuantity);

            if (newQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient inventory quantity"
                );
            }

            if (inventory.getReservedQuantity().compareTo(newQuantity) > 0) {
                throw new IllegalArgumentException(
                        "Movement would reduce inventory below reserved quantity"
                );
            }
        }

        inventory.update(
                newQuantity,
                inventory.getReservedQuantity()
        );

        inventoryRepository.save(inventory);

        LocalDateTime now = LocalDateTime.now();

        StockMovement movement = new StockMovement(
                UUID.randomUUID(),
                inventory,
                request.movementType(),
                movementQuantity,
                request.referenceNumber(),
                request.notes(),
                request.movementDate() != null
                        ? request.movementDate()
                        : now,
                now
        );

        return toResponse(stockMovementRepository.save(movement));
    }

    private boolean isIncoming(MovementType movementType) {
        return movementType == MovementType.PURCHASE
                || movementType == MovementType.TRANSFER_IN
                || movementType == MovementType.ADJUSTMENT_IN
                || movementType == MovementType.MANUFACTURING_IN;
    }

    private StockMovementResponse toResponse(StockMovement movement) {
        Inventory inventory = movement.getInventory();

        return new StockMovementResponse(
                movement.getId(),
                inventory.getId(),
                inventory.getWarehouse().getId(),
                inventory.getProduct().getId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getReferenceNumber(),
                movement.getNotes(),
                movement.getMovementDate(),
                movement.getCreatedAt()
        );
    }
}