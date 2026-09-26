package com.jewelvaulterp.integration.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationPurchaseResponse(
        UUID id,
        UUID companyId,
        UUID supplierId,
        UUID warehouseId,
        String purchaseNumber,
        LocalDateTime purchaseDate,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
