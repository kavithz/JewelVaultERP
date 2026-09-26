package com.jewelvaulterp.integration.dto;

import com.jewelvaulterp.invoice.entity.InvoiceStatus;
import com.jewelvaulterp.invoice.entity.InvoiceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationInvoiceResponse(
        UUID id,
        UUID companyId,
        String invoiceNumber,
        LocalDateTime invoiceDate,
        InvoiceType invoiceType,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        InvoiceStatus status,
        String notes,
        UUID customerId,
        UUID supplierId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
