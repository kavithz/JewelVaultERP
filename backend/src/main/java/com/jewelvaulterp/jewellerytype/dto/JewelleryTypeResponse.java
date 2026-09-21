package com.jewelvaulterp.jewellerytype.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record JewelleryTypeResponse(
        UUID id,
        UUID companyId,
        String name,
        String code,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}