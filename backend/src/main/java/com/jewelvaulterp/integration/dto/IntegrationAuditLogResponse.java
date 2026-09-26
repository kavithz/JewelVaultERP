package com.jewelvaulterp.integration.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record IntegrationAuditLogResponse(
        UUID id,
        UUID companyId,
        UUID userId,
        String action,
        String entityType,
        UUID entityId,
        String description,
        String ipAddress,
        LocalDateTime createdAt
) {
}
