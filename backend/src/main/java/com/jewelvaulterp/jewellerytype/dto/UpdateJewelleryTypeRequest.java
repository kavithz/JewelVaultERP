package com.jewelvaulterp.jewellerytype.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateJewelleryTypeRequest(

        @NotBlank
        String name,

        @NotBlank
        String code,

        String description
) {
}