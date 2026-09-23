package com.jewelvaulterp.sale.dto;

import com.jewelvaulterp.sale.entity.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleResponse(
        UUID id,
        UUID companyId,
        UUID customerId,
        UUID warehouseId,
        String saleNumber,
        LocalDateTime saleDate,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        SaleStatus status,
        List<SaleItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}