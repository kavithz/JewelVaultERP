package com.jewelvaulterp.integration.dto;

import com.jewelvaulterp.tax.entity.TaxType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationTaxResponse(
        UUID id,
        UUID companyId,
        String name,
        String code,
        TaxType taxType,
        BigDecimal rate,
        String description,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
