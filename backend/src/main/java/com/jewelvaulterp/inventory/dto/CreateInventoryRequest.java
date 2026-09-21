package com.jewelvaulterp.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInventoryRequest(
        @NotNull UUID warehouseId,
        @NotNull UUID productId,
        @NotNull @DecimalMin("0.000") BigDecimal quantity,
        @NotNull @DecimalMin("0.000") BigDecimal reservedQuantity
) {
}