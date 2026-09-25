package com.jewelvaulterp.financial.dto;

import java.math.BigDecimal;

public record PurchaseSummaryResponse(
        BigDecimal totalPurchases,
        BigDecimal totalPaid,
        BigDecimal totalOutstanding,
        long purchaseCount
) {
}
