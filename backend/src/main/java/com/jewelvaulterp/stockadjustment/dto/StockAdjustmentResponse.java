package com.jewelvaulterp.stockadjustment.dto;

import com.jewelvaulterp.stockadjustment.entity.StockAdjustmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record StockAdjustmentResponse(
        UUID id,
        UUID companyId,
        UUID warehouseId,
        String adjustmentNumber,
        LocalDateTime adjustmentDate,
        StockAdjustmentStatus status,
        String notes,
        List<StockAdjustmentItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}