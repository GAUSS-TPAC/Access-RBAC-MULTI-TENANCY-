package com.yow.access.services;

import com.yow.access.entities.Resource;
import com.yow.access.entities.UserRoleResource;
import com.yow.access.exceptions.AccessDeniedException;
import com.yow.access.repositories.ResourceRepository;
import com.yow.access.repositories.UserRoleResourceRepository;
import org.springframework.stereotype.Service;

import java.util.*;

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
     * RBAC check on a specific resource (hierarchy-aware).
     * @throws AccessDeniedException if user lacks the permission
     * @throws IllegalStateException if resource not found
     */
    public void checkPermission(
            UUID userId,
            UUID resourceId,
            String permissionName
    ) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalStateException("Resource not found"));

        if (!hasPermission(userId, permissionName, resource)) {
            throw new AccessDeniedException("Permission denied: " + permissionName);
        }
    }

    /**
     * RBAC check without resource scope (SYSTEM / GLOBAL permissions).
     * @throws AccessDeniedException if user lacks the permission
     */
    public void checkGlobalPermission(
            UUID userId,
            String permissionName
    ) {
        boolean allowed = urrRepository.findAllByUserId(userId)
                .stream()
                .anyMatch(urr -> urr.getRole().getPermissions()
                        .stream()
                        .anyMatch(p -> p.getName().equals(permissionName)));

        if (!allowed) {
            throw new AccessDeniedException("Permission denied: " + permissionName);
        }
    }

    /* ===== INTERNAL RBAC ENGINE ===== */
    /**
     * Core RBAC permission check with hierarchy inheritance.
     * Walks up the resource tree until a matching permission is found.
     * Protected against circular references.
     */
    public boolean hasPermission(
            UUID userId,
            String permissionName,
            Resource target
    ) {
        // Early returns for invalid inputs
        if (target == null) {
            return false;
        }

        List<UserRoleResource> bindings = urrRepository.findAllByUserId(userId);
        System.out.println("DEBUG: Check Permission '" + permissionName + "' for User " + userId + " on Resource " + target.getName() + " (" + target.getId() + ")");
        System.out.println("DEBUG: Found " + bindings.size() + " bindings for user.");

        // No bindings = no permissions
        if (bindings == null || bindings.isEmpty()) {
            System.out.println("DEBUG: No bindings found. Access denied.");
            return false;
        }

        // Walks up the resource tree until a matching permission is found.
        // Protected against circular references.
        // ADMIN role has full access.
        for (UserRoleResource urr : bindings) {
            if (urr.getRole().getName().equals("ADMIN")) {
                System.out.println("DEBUG: ADMIN role found. Access granted.");
                return true;
            }
        }

        Resource current = target;
        Set<UUID> visitedResources = new HashSet<>(); // Anti-loop protection

        // Walk up the resource hierarchy
        while (current != null) {
            System.out.println("DEBUG: Checking at resource level: " + current.getName() + " (" + current.getId() + ")");
            // Detect and break circular references
            if (visitedResources.contains(current.getId())) {
                System.out.println("DEBUG: Circular reference detected.");
                break; // Circular reference detected
            }
            visitedResources.add(current.getId());

            // Check all bindings for current resource level
            for (UserRoleResource urr : bindings) {
                // Skip bindings for different resources
                if (!urr.getResource().getId().equals(current.getId())) {
                    continue;
                }
                
                System.out.println("DEBUG: Matching resource binding found. Role: " + urr.getRole().getName());
                
                // Debug permissions
                urr.getRole().getPermissions().forEach(p -> System.out.println("DEBUG:   - Has permission: " + p.getName()));

                // Check if role has the required permission
                boolean hasPermission = urr.getRole().getPermissions()
                        .stream()
                        .anyMatch(p -> p.getName().equals(permissionName));

                if (hasPermission) {
                    System.out.println("DEBUG: Permission MATCHED! Access granted.");
                    return true; // Permission granted
                }
            }

            // Move up to parent resource
            current = current.getParent();
            if (current != null) {
                System.out.println("DEBUG: Moving up to parent: " + current.getName());
            } else {
                System.out.println("DEBUG: No parent (Root reached).");
            }
        }
        
        System.out.println("DEBUG: End of hierarchy reached. Access denied.");
        return false; // No matching permission found in hierarchy
    }
}