package com.jewelvaulterp.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreatePurchaseRequest(
        @NotNull UUID companyId,
        @NotNull UUID supplierId,
        @NotNull UUID warehouseId,

        @NotBlank
        @Size(max = 100)
        String purchaseNumber,

        LocalDateTime purchaseDate,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal taxAmount,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal discountAmount,

        @NotEmpty
        List<@Valid CreatePurchaseItemRequest> items
) {
}