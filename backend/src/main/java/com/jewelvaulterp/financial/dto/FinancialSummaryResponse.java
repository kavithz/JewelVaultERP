package com.jewelvaulterp.financial.dto;

import java.math.BigDecimal;

public record FinancialSummaryResponse(
        BigDecimal totalSales,
        BigDecimal totalPurchases,
        BigDecimal totalExpenses,
        BigDecimal receivablesOutstanding,
        BigDecimal payablesOutstanding,
        BigDecimal taxAmount,
        BigDecimal netCashFlow
) {
}
