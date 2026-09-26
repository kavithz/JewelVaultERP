package com.jewelvaulterp.integration.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationSupplierResponse(
        UUID id,
        UUID companyId,
        String name,
        String code,
        String contactPerson,
        String phone,
        String email,
        String address,
        String taxNumber,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
