package com.jewelvaulterp.purity.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePurityRequest(

        @NotNull
        UUID companyId,

        @NotBlank
        String name,

        @NotBlank
        String code,

        @NotNull
        @DecimalMin("0.001")
        @DecimalMax("1000.000")
        BigDecimal fineness,

        String description
) {
}