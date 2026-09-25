package com.jewelvaulterp.receivable.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReceivableRequest(

        @NotNull
        UUID companyId,

        @NotNull
        UUID customerId,

        UUID invoiceId,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        LocalDateTime dueDate
) {
}