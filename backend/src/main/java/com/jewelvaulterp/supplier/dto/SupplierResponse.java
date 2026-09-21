package com.jewelvaulterp.supplier.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierResponse(
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