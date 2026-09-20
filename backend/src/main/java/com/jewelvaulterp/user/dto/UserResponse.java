package com.jewelvaulterp.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID companyId,
        String username,
        String email,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
