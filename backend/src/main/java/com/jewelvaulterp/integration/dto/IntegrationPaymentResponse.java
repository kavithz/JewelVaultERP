package com.jewelvaulterp.integration.dto;

import com.jewelvaulterp.payment.entity.PaymentMethod;
import com.jewelvaulterp.payment.entity.PaymentStatus;
import com.jewelvaulterp.payment.entity.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationPaymentResponse(
        UUID id,
        UUID companyId,
        String paymentNumber,
        LocalDateTime paymentDate,
        PaymentType paymentType,
        PaymentMethod paymentMethod,
        BigDecimal amount,
        String referenceNumber,
        String notes,
        PaymentStatus status,
        UUID customerId,
        UUID supplierId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
