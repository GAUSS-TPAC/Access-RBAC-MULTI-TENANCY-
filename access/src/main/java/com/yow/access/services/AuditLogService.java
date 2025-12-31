package com.yow.access.services;

import com.yow.access.entities.*;
import com.yow.access.repositories.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for audit logging.
 *
 * Author: Alan Tchapda
 * Date: 2025-12-30
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(
            Tenant tenant,
            AppUser user,
            Resource resource,
            String action,
            String targetType,
            Long targetId,
            String outcome,
            String message,
            String ipAddress,
            String userAgent
    ) {
        AuditLog log =
                AuditLogFactory.create(
                        tenant,
                        user,
                        resource,
                        action,
                        targetType,
                        targetId,
                        outcome,
                        message,
                        ipAddress,
                        userAgent
                );

        auditLogRepository.save(log);
    }

}
