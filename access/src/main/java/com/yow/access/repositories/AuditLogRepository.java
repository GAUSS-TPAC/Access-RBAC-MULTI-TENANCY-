package com.yow.access.repositories;

import com.yow.access.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findAllByTenantId(UUID tenantId);

    List<AuditLog> findAllByUserId(UUID userId);
}

