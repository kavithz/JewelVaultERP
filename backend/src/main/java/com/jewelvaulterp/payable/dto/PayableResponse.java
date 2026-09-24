package com.jewelvaulterp.payable.dto;

import com.jewelvaulterp.payable.entity.PayableStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PayableResponse(
        UUID id,
        UUID companyId,
        UUID supplierId,
        UUID invoiceId,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount,
        LocalDateTime dueDate,
        PayableStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}