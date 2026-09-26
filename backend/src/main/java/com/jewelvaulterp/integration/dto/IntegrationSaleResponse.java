package com.jewelvaulterp.integration.dto;

import com.jewelvaulterp.sale.entity.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationSaleResponse(
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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
