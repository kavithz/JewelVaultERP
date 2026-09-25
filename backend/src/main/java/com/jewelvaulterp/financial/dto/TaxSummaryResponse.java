package com.jewelvaulterp.financial.dto;

import java.math.BigDecimal;

public record TaxSummaryResponse(
        BigDecimal totalTax,
        BigDecimal salesTax,
        BigDecimal purchaseTax,
        long taxRecordCount
) {
}
