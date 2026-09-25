package com.jewelvaulterp.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateRoleRequest(
        @NotNull UUID companyId,
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description
) {
}
