package com.jewelvaulterp.accounting.report.dto;

import com.jewelvaulterp.accounting.entity.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceResponse(
        UUID accountId,
        String accountCode,
        String accountName,
        AccountType accountType,
        BigDecimal debitTotal,
        BigDecimal creditTotal,
        BigDecimal balance
) {
}