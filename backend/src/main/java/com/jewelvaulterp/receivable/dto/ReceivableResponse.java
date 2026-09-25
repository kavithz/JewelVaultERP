package com.jewelvaulterp.receivable.dto;

import com.jewelvaulterp.receivable.entity.ReceivableStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReceivableResponse(
        UUID id,
        UUID companyId,
        UUID customerId,
        UUID invoiceId,
        BigDecimal amount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount,
        LocalDateTime dueDate,
        ReceivableStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}