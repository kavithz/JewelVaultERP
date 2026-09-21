package com.jewelvaulterp.purity.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdatePurityRequest(

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