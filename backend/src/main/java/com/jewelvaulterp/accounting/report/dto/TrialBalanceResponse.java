package com.jewelvaulterp.accounting.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record TrialBalanceResponse(
        List<AccountBalanceResponse> accounts,
        BigDecimal totalDebits,
        BigDecimal totalCredits
) {
}