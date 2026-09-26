package com.jewelvaulterp.integration.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationBranchResponse(
        UUID id,
        UUID companyId,
        String name,
        String code,
        String address,
        String city,
        String countryCode,
        String phone,
        String email,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
