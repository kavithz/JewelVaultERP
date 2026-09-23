package com.jewelvaulterp.accounting.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateJournalEntryLineRequest(
        @NotNull UUID accountId,
        @Size(max = 500) String description,
        @NotNull @DecimalMin("0.00") BigDecimal debitAmount,
        @NotNull @DecimalMin("0.00") BigDecimal creditAmount
) {}