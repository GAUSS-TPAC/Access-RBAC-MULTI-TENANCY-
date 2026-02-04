package com.yow.access.controllers;

import com.yow.access.entities.AppUser;
import com.yow.access.entities.AuditLog;
import com.yow.access.repositories.AuditLogRepository;
import com.yow.access.repositories.UserRoleResourceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;
    private final UserRoleResourceRepository urrRepository;

    public AuditLogController(AuditLogRepository auditLogRepository, UserRoleResourceRepository urrRepository) {
        this.auditLogRepository = auditLogRepository;
        this.urrRepository = urrRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAuditLogs(
            @AuthenticationPrincipal AppUser currentUser,
            @RequestParam(required = false) UUID tenantId
    ) {
        // Obtenir les rôles de l'utilisateur
        List<String> roles = urrRepository.findAllByUserId(currentUser.getId())
                .stream()
                .map(urr -> urr.getRole().getName())
                .collect(Collectors.toList());

        boolean isSuperAdmin = roles.contains("ADMIN");
        boolean isTenantAdmin = roles.contains("TENANT_ADMIN");

        List<AuditLog> logs;

        if (isSuperAdmin) {
            if (tenantId != null) {
                logs = auditLogRepository.findAllByTenantId(tenantId);
            } else {
                logs = auditLogRepository.findAll();
            }
        } else if (isTenantAdmin) {
            // Un admin de tenant ne peut voir que les logs de son propre tenant
            // Pour simplifier, on récupère le tenantId depuis sa première assignation de rôle
            UUID userTenantId = urrRepository.findAllByUserId(currentUser.getId()).stream()
                    .map(urr -> urr.getResource().getTenant().getId())
                    .findFirst()
                    .orElse(null);

            if (userTenantId == null) {
                return ResponseEntity.status(403).build();
            }
            logs = auditLogRepository.findAllByTenantId(userTenantId);
        } else {
            // Les autres utilisateurs ne voient que leurs propres logs
            logs = auditLogRepository.findAllByUserId(currentUser.getId());
        }

        List<Map<String, Object>> response = logs.stream()
                .map(log -> Map.<String, Object>of(
                        "id", log.getId(),
                        "action", log.getAction(),
                        "timestamp", log.getTimestamp(),
                        "username", log.getUser() != null ? log.getUser().getUsername() : "Système",
                        "resourceType", log.getResource() != null ? log.getResource().getType() : (log.getTargetType() != null ? log.getTargetType() : ""),
                        "outcome", log.getOutcome() != null ? log.getOutcome() : "",
                        "message", log.getMessage() != null ? log.getMessage() : ""
                ))
                .sorted((a, b) -> ((java.time.Instant) b.get("timestamp")).compareTo((java.time.Instant) a.get("timestamp")))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
