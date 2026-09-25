package com.jewelvaulterp.financial.dto;

import java.math.BigDecimal;

public record CashFlowSummaryResponse(
        BigDecimal cashInflow,
        BigDecimal cashOutflow,
        BigDecimal netCashFlow
) {
}
