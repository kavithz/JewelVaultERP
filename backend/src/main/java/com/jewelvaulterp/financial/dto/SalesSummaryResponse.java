package com.jewelvaulterp.financial.dto;

import java.math.BigDecimal;

public record SalesSummaryResponse(
        BigDecimal totalSales,
        BigDecimal totalPaid,
        BigDecimal totalOutstanding,
        long invoiceCount
) {
}
