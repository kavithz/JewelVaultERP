package com.jewelvaulterp.role.dto;

import jakarta.validation.constraints.Size;

public record UpdateRoleRequest(
        @Size(max = 100) String name,
        @Size(max = 255) String description,
        Boolean active
) {
}
