package com.jewelvaulterp.accounting.report.dto;

import java.math.BigDecimal;

public record BalanceSheetResponse(
        BigDecimal assets,
        BigDecimal liabilities,
        BigDecimal equity,
        BigDecimal netProfit,
        BigDecimal totalLiabilitiesAndEquity
) {
}