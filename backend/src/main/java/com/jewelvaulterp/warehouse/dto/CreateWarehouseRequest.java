package com.jewelvaulterp.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateWarehouseRequest(

        @NotNull
        UUID branchId,

        @NotBlank
        String name,

        @NotBlank
        String code,

        String address,

        String description
) {
}