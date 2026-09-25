package com.jewelvaulterp.audit.dto;

import com.jewelvaulterp.audit.entity.AuditAction;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogSearchRequest(
        UUID companyId,
        UUID userId,
        AuditAction action,
        String entityType,
        UUID entityId,
        LocalDateTime from,
        LocalDateTime to
) {
}
