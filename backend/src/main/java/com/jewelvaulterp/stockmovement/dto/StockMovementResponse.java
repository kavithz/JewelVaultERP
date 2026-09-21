package com.jewelvaulterp.stockmovement.dto;

import com.jewelvaulterp.stockmovement.entity.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockMovementResponse(
        UUID id,
        UUID inventoryId,
        UUID warehouseId,
        UUID productId,
        MovementType movementType,
        BigDecimal quantity,
        String referenceNumber,
        String notes,
        LocalDateTime movementDate,
        LocalDateTime createdAt
) {
}