package com.jewelvaulterp.financial.dto;

import java.math.BigDecimal;

public record ReceivablesSummaryResponse(
        BigDecimal totalReceivables,
        BigDecimal totalPaid,
        BigDecimal totalOutstanding,
        BigDecimal overdueAmount,
        long receivableCount
) {
}
