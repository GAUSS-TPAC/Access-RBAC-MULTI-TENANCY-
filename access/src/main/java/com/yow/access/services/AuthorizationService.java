package com.yow.access.services;

import com.yow.access.entities.Resource;
import com.yow.access.entities.UserRoleResource;
import com.yow.access.repositories.ResourceRepository;
import com.yow.access.repositories.UserRoleResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Central RBAC decision engine.
 *
 * Author: Alan Tchapda
 * Date: 2025-12-30
 */
@Service
public class AuthorizationService {

    private final UserRoleResourceRepository urrRepository;
    private final ResourceRepository resourceRepository;

    public AuthorizationService(
            UserRoleResourceRepository urrRepository,
            ResourceRepository resourceRepository
    ) {
        this.urrRepository = urrRepository;
        this.resourceRepository = resourceRepository;
    }

    /**
     * Core authorization check.
     */
    public boolean hasPermission(
            UUID userId,
            String permissionName,
            Resource targetResource
    ) {
        // 1️⃣ Load all role bindings for user
        List<UserRoleResource> bindings =
                urrRepository.findAllByUserId(userId);

        // 2️⃣ Walk up resource hierarchy
        Resource current = targetResource;

        while (current != null) {
            for (UserRoleResource urr : bindings) {

                // Match resource scope
                if (!urr.getResource().getId().equals(current.getId())) {
                    continue;
                }

                // Role must contain permission
                boolean roleHasPermission =
                        urr.getRole()
                                .getPermissions()
                                .stream()
                                .anyMatch(p -> p.getName().equals(permissionName));

                if (roleHasPermission) {
                    return true;
                }
            }

            current = current.getParent();
        }

        return false;
    }
}
