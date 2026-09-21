package com.jewelvaulterp.stockmovement.dto;

import com.jewelvaulterp.stockmovement.entity.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateStockMovementRequest(
        @NotNull UUID inventoryId,
        @NotNull MovementType movementType,
        @NotNull @DecimalMin("0.001") BigDecimal quantity,
        @Size(max = 100) String referenceNumber,
        @Size(max = 500) String notes,
        LocalDateTime movementDate
) {
}