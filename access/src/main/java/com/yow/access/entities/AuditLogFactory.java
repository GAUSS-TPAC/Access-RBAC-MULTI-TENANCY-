package com.yow.access.entities;

/**
 * Factory for AuditLog creation.
 */
public final class AuditLogFactory {

    private AuditLogFactory() {
    }

    public static AuditLog create(
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
        AuditLog log = new AuditLog();
        log.setTenant(tenant);
        log.setUser(user);
        log.setResource(resource);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOutcome(outcome);
        log.setMessage(message);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        return log;
    }
}
