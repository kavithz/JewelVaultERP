package com.jewelvaulterp.purity.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurityResponse(
        UUID id,
        UUID companyId,
        String name,
        String code,
        BigDecimal fineness,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}