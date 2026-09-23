package com.jewelvaulterp.invoice.dto;

import com.jewelvaulterp.invoice.entity.InvoiceStatus;
import com.jewelvaulterp.invoice.entity.InvoiceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID companyId,
        String invoiceNumber,
        LocalDateTime invoiceDate,
        InvoiceType invoiceType,
        UUID customerId,
        UUID supplierId,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        InvoiceStatus status,
        String notes,
        List<InvoiceItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}