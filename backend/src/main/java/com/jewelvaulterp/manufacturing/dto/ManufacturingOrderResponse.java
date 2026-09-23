package com.jewelvaulterp.manufacturing.dto;

import com.jewelvaulterp.manufacturing.entity.ManufacturingOrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ManufacturingOrderResponse(
        UUID id,
        UUID companyId,
        UUID warehouseId,
        String orderNumber,
        LocalDateTime orderDate,
        ManufacturingOrderStatus status,
        String notes,
        List<ManufacturingOrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
