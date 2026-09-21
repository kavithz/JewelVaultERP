package com.jewelvaulterp.jewellerytype.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateJewelleryTypeRequest(

        @NotNull
        UUID companyId,

        @NotBlank
        String name,

        @NotBlank
        String code,

        String description
) {
}