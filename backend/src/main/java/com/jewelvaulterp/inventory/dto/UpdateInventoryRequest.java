package com.jewelvaulterp.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateInventoryRequest(
        @NotNull @DecimalMin("0.000") BigDecimal quantity,
        @NotNull @DecimalMin("0.000") BigDecimal reservedQuantity
) {
}