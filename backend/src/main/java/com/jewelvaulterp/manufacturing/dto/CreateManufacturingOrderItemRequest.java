package com.jewelvaulterp.manufacturing.dto;

import com.jewelvaulterp.manufacturing.entity.ManufacturingItemType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateManufacturingOrderItemRequest(

        @NotNull
        UUID productId,

        @NotNull
        @DecimalMin("0.001")
        BigDecimal quantity,

        @NotNull
        ManufacturingItemType itemType
) {
}