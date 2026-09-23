package com.jewelvaulterp.manufacturing.dto;

import com.jewelvaulterp.manufacturing.entity.ManufacturingItemType;

import java.math.BigDecimal;
import java.util.UUID;

public record ManufacturingOrderItemResponse(
        UUID id,
        UUID productId,
        BigDecimal quantity,
        ManufacturingItemType itemType
) {
}