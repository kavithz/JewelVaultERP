package com.jewelvaulterp.branch.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BranchResponse(
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