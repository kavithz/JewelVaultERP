package com.jewelvaulterp.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID warehouseId,
        UUID productId,
        BigDecimal quantity,
        BigDecimal reservedQuantity,
        BigDecimal availableQuantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}