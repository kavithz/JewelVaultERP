package com.jewelvaulterp.purchase.dto;

import com.jewelvaulterp.purchase.entity.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PurchaseResponse(
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
        PurchaseStatus status,
        List<PurchaseItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}