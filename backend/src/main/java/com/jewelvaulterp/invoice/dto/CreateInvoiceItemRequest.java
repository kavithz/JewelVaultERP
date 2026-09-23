package com.jewelvaulterp.invoice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInvoiceItemRequest(

        @NotNull
        UUID productId,

        @NotNull
        @DecimalMin("0.001")
        BigDecimal quantity,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal unitPrice,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal taxAmount,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal discountAmount
) {
}