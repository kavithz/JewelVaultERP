package com.jewelvaulterp.role.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignRoleRequest(
        @NotNull UUID roleId
) {
}
