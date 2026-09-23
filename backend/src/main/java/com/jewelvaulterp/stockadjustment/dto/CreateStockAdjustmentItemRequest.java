package com.jewelvaulterp.stockadjustment.dto;

import com.jewelvaulterp.stockadjustment.entity.AdjustmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateStockAdjustmentItemRequest(

        @NotNull
        UUID productId,

        @NotNull
        @DecimalMin("0.001")
        BigDecimal quantity,

        @NotNull
        AdjustmentType adjustmentType
) {
}