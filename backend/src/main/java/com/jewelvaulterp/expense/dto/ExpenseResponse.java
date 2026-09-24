package com.jewelvaulterp.expense.dto;

import com.jewelvaulterp.expense.entity.ExpensePaymentMethod;
import com.jewelvaulterp.expense.entity.ExpenseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        UUID companyId,
        UUID branchId,
        UUID categoryId,
        String description,
        BigDecimal amount,
        LocalDateTime expenseDate,
        ExpensePaymentMethod paymentMethod,
        String referenceNumber,
        ExpenseStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
