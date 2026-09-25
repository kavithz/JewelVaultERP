package com.jewelvaulterp.financial.dto;

import java.math.BigDecimal;

public record PayablesSummaryResponse(
        BigDecimal totalPayables,
        BigDecimal totalPaid,
        BigDecimal totalOutstanding,
        BigDecimal overdueAmount,
        long payableCount
) {
}
