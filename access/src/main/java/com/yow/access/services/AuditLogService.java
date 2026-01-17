package com.yow.access.services;

import com.yow.access.entities.*;
import com.yow.access.repositories.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void log(
            Tenant tenant,
            AppUser user,
            Resource resource,
            String action,
            String targetType,
            UUID targetId,
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

        repository.save(log);
    }
}
