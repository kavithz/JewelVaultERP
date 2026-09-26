package com.jewelvaulterp.integration.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationProductResponse(
        UUID id,
        UUID companyId,
        String sku,
        String name,
        String jewelleryType,
        String metalType,
        String purity,
        BigDecimal grossWeight,
        BigDecimal netWeight,
        BigDecimal makingCharge,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
