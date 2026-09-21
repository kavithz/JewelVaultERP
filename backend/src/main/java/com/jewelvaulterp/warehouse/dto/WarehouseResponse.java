package com.jewelvaulterp.warehouse.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        UUID branchId,
        UUID companyId,
        String name,
        String code,
        String address,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}