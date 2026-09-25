package com.jewelvaulterp.audit.dto;

import com.jewelvaulterp.audit.entity.AuditAction;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID companyId,
        UUID userId,
        AuditAction action,
        String entityType,
        UUID entityId,
        String description,
        String oldValues,
        String newValues,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
) {
}
