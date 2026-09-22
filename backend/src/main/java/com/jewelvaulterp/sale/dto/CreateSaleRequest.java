package com.jewelvaulterp.sale.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateSaleRequest(
        @NotNull UUID companyId,
        @NotNull UUID customerId,
        @NotNull UUID warehouseId,
        @NotBlank @Size(max = 100) String saleNumber,
        LocalDateTime saleDate,
        @NotNull @DecimalMin("0.00") BigDecimal taxAmount,
        @NotNull @DecimalMin("0.00") BigDecimal discountAmount,
        @NotEmpty List<@Valid CreateSaleItemRequest> items
) {
}