package com.jewelvaulterp.audit.controller;

import com.jewelvaulterp.audit.dto.AuditLogResponse;
import com.jewelvaulterp.audit.dto.CreateAuditLogRequest;
import com.jewelvaulterp.audit.entity.AuditAction;
import com.jewelvaulterp.audit.service.AuditLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuditLogResponse create(@Valid @RequestBody CreateAuditLogRequest request) {
        return auditLogService.create(request);
    }

    @GetMapping("/{id}")
    public AuditLogResponse getById(@PathVariable UUID id) {
        return auditLogService.getById(id);
    }

    @GetMapping("/company/{companyId}")
    public List<AuditLogResponse> getByCompany(@PathVariable UUID companyId) {
        return auditLogService.getByCompany(companyId);
    }

    @GetMapping("/company/{companyId}/user/{userId}")
    public List<AuditLogResponse> getByCompanyAndUser(
            @PathVariable UUID companyId,
            @PathVariable UUID userId
    ) {
        return auditLogService.getByCompanyAndUser(companyId, userId);
    }

    @GetMapping("/company/{companyId}/entity/{entityType}/{entityId}")
    public List<AuditLogResponse> getByCompanyAndEntity(
            @PathVariable UUID companyId,
            @PathVariable String entityType,
            @PathVariable UUID entityId
    ) {
        return auditLogService.getByCompanyAndEntity(companyId, entityType, entityId);
    }

    @GetMapping("/company/{companyId}/action/{action}")
    public List<AuditLogResponse> getByCompanyAndAction(
            @PathVariable UUID companyId,
            @PathVariable AuditAction action
    ) {
        return auditLogService.getByCompanyAndAction(companyId, action);
    }
}
