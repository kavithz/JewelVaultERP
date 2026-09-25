package com.jewelvaulterp.tax.dto;

import com.jewelvaulterp.tax.entity.TaxType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTaxRequest(
        @NotNull UUID companyId,
        @NotBlank String name,
        @NotBlank String code,
        @NotNull TaxType taxType,
        @NotNull @DecimalMin("0.00") BigDecimal rate,
        String description
) {
}