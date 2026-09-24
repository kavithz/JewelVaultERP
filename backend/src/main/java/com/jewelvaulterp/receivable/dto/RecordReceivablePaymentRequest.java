package com.jewelvaulterp.receivable.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecordReceivablePaymentRequest(

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount
) {
}