package com.jewelvaulterp.gemstone.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateGemstoneRequest(

        @NotBlank
        String name,

        @NotBlank
        String code,

        String category,

        String color,

        String description
) {
}