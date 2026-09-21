package com.jewelvaulterp.warehouse.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateWarehouseRequest(

        @NotBlank
        String name,

        @NotBlank
        String code,

        String address,

        String description
) {
}