package com.jewelvaulterp.stockadjustment.dto;

import com.jewelvaulterp.stockadjustment.entity.AdjustmentType;

import java.math.BigDecimal;
import java.util.UUID;

public record StockAdjustmentItemResponse(
        UUID id,
        UUID productId,
        BigDecimal quantity,
        AdjustmentType adjustmentType
) {
}