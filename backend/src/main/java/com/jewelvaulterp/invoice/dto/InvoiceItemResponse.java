package com.jewelvaulterp.invoice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InvoiceItemResponse(
        UUID id,
        UUID productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalPrice
) {
}