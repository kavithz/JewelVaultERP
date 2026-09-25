package com.jewelvaulterp.audit.service;

import com.jewelvaulterp.audit.dto.AuditLogResponse;
import com.jewelvaulterp.audit.dto.CreateAuditLogRequest;
import com.jewelvaulterp.audit.entity.AuditLog;
import com.jewelvaulterp.audit.repository.AuditLogRepository;
import com.jewelvaulterp.company.entity.Company;
import com.jewelvaulterp.company.repository.CompanyRepository;
import com.jewelvaulterp.user.entity.User;
import com.jewelvaulterp.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            CompanyRepository companyRepository,
            UserRepository userRepository
    ) {
        this.auditLogRepository = auditLogRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public AuditLogResponse create(CreateAuditLogRequest request) {
        Company company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        User user = null;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (!company.getId().equals(user.getCompany().getId())) {
                throw new IllegalArgumentException("User does not belong to the supplied company");
            }
        }

        AuditLog auditLog = new AuditLog(
                UUID.randomUUID(),
                company,
                user,
                request.action(),
                request.entityType(),
                request.entityId(),
                request.description(),
                request.oldValues(),
                request.newValues(),
                request.ipAddress(),
                request.userAgent(),
                LocalDateTime.now()
        );

        return toResponse(auditLogRepository.save(auditLog));
    }

    public AuditLogResponse getById(UUID id) {
        return toResponse(auditLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Audit log not found")));
    }

    public List<AuditLogResponse> getByCompany(UUID companyId) {
        validateCompanyExists(companyId);
        return auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuditLogResponse> getByCompanyAndUser(UUID companyId, UUID userId) {
        validateCompanyExists(companyId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!companyId.equals(user.getCompany().getId())) {
            throw new IllegalArgumentException("User does not belong to the supplied company");
        }

        return auditLogRepository.findByCompanyIdAndUserIdOrderByCreatedAtDesc(companyId, userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuditLogResponse> getByCompanyAndEntity(UUID companyId, String entityType, UUID entityId) {
        validateCompanyExists(companyId);

        return auditLogRepository.findByCompanyIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        companyId,
                        entityType,
                        entityId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<AuditLogResponse> getByCompanyAndAction(UUID companyId, com.jewelvaulterp.audit.entity.AuditAction action) {
        validateCompanyExists(companyId);

        return auditLogRepository.findByCompanyIdAndActionOrderByCreatedAtDesc(companyId, action)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateCompanyExists(UUID companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getCompany().getId(),
                auditLog.getUser() != null ? auditLog.getUser().getId() : null,
                auditLog.getAction(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getDescription(),
                auditLog.getOldValues(),
                auditLog.getNewValues(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getCreatedAt()
        );
    }
}
