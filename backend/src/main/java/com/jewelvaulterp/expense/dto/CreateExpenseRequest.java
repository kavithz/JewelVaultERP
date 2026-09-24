package com.jewelvaulterp.expense.dto;

import com.jewelvaulterp.expense.entity.ExpensePaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateExpenseRequest(
        @NotNull UUID companyId,
        UUID branchId,
        @NotNull UUID categoryId,
        @NotBlank String description,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull LocalDateTime expenseDate,
        @NotNull ExpensePaymentMethod paymentMethod,
        String referenceNumber
) {
}
