package com.jewelvaulterp.financial.dto;

import java.math.BigDecimal;

public record ExpenseSummaryResponse(
        BigDecimal totalExpenses,
        BigDecimal totalPaid,
        BigDecimal totalOutstanding,
        long expenseCount
) {
}
