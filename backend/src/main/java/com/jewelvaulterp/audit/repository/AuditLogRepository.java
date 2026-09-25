package com.jewelvaulterp.audit.repository;

import com.jewelvaulterp.audit.entity.AuditAction;
import com.jewelvaulterp.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);

    List<AuditLog> findByCompanyIdAndUserIdOrderByCreatedAtDesc(
            UUID companyId,
            UUID userId
    );

    List<AuditLog> findByCompanyIdAndActionOrderByCreatedAtDesc(
            UUID companyId,
            AuditAction action
    );

    List<AuditLog> findByCompanyIdAndEntityTypeAndEntityIdOrderByCreatedAtDesc(
            UUID companyId,
            String entityType,
            UUID entityId
    );

    @Query("""
            SELECT a
            FROM AuditLog a
            WHERE a.company.id = :companyId
            ORDER BY a.createdAt DESC
            """)
    List<AuditLog> findRecentByCompanyId(
            @Param("companyId") UUID companyId
    );
}
