package com.jewelvaulterp.invoice.dto;

import com.jewelvaulterp.invoice.entity.InvoiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateInvoiceRequest(

        @NotNull
        UUID companyId,

        @NotBlank
        @Size(max = 100)
        String invoiceNumber,

        @NotNull
        InvoiceType invoiceType,

        UUID customerId,

        UUID supplierId,

        @Size(max = 500)
        String notes,

        @NotEmpty
        List<@Valid CreateInvoiceItemRequest> items
) {
}