package com.jewelvaulterp.accounting.report.dto;

import java.math.BigDecimal;

public record ProfitLossResponse(
        BigDecimal revenue,
        BigDecimal expenses,
        BigDecimal netProfit
) {
}