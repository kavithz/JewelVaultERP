package com.jewelvaulterp.integration.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationCustomerResponse(
        UUID id,
        UUID companyId,
        String name,
        String code,
        String phone,
        String email,
        String address,
        String taxNumber,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
