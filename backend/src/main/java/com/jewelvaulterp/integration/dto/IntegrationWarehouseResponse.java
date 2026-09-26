package com.jewelvaulterp.integration.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationWarehouseResponse(
        UUID id,
        UUID companyId,
        UUID branchId,
        String name,
        String code,
        String address,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
