package com.jewelvaulterp.role.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        UUID companyId,
        String name,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
