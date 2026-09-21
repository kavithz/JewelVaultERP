package com.jewelvaulterp.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateProductRequest(

        @NotBlank
        String sku,

        @NotBlank
        String name,

        @NotBlank
        String jewelleryType,

        @NotBlank
        String metalType,

        @NotBlank
        String purity,

        @NotNull
        @Positive
        BigDecimal grossWeight,

        @NotNull
        @Positive
        BigDecimal netWeight,

        @NotNull
        @PositiveOrZero
        BigDecimal makingCharge
) {
}