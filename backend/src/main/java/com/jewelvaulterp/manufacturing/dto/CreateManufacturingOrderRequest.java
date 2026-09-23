package com.jewelvaulterp.manufacturing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateManufacturingOrderRequest(

        @NotNull
        UUID companyId,

        @NotNull
        UUID warehouseId,

        @NotBlank
        @Size(max = 100)
        String orderNumber,

        @Size(max = 500)
        String notes,

        @NotEmpty
        List<@Valid CreateManufacturingOrderItemRequest> items
) {
}