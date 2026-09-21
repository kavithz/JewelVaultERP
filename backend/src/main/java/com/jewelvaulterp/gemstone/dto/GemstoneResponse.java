package com.jewelvaulterp.gemstone.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GemstoneResponse(
        UUID id,
        UUID companyId,
        String name,
        String code,
        String category,
        String color,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}