package com.jewelvaulterp.audit.dto;

import com.jewelvaulterp.audit.entity.AuditAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateAuditLogRequest(
        @NotNull UUID companyId,
        UUID userId,
        @NotNull AuditAction action,
        @NotBlank @Size(max = 100) String entityType,
        UUID entityId,
        @Size(max = 1000) String description,
        String oldValues,
        String newValues,
        @Size(max = 100) String ipAddress,
        String userAgent
) {
}
