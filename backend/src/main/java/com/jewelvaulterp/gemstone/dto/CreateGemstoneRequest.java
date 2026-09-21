package com.jewelvaulterp.gemstone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateGemstoneRequest(

        @NotNull
        UUID companyId,

        @NotBlank
        String name,

        @NotBlank
        String code,

        String category,

        String color,

        String description
) {
}