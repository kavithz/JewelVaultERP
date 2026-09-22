package com.jewelvaulterp.customer.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
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