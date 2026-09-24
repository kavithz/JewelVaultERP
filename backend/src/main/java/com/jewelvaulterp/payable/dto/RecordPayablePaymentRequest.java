package com.jewelvaulterp.payable.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RecordPayablePaymentRequest(

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal amount
) {
}