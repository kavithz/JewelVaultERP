package com.jewelvaulterp.payment.dto;

import com.jewelvaulterp.payment.entity.PaymentMethod;
import com.jewelvaulterp.payment.entity.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentRequest(

        @NotNull
        UUID companyId,

        @NotBlank
        @Size(max = 100)
        String paymentNumber,

        @NotNull
        PaymentType paymentType,

        @NotNull
        PaymentMethod paymentMethod,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @Size(max = 100)
        String referenceNumber,

        @Size(max = 500)
        String notes,

        UUID customerId,

        UUID supplierId
) {
}