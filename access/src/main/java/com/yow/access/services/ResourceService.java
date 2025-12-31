package com.yow.access.services;

import com.yow.access.entities.*;
import com.yow.access.repositories.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service responsible for resource hierarchy management.
 *
 * Author: Alan Tchapda
 * Date: 2025-12-30
 */
@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final AuthorizationService authorizationService;

    public ResourceService(
            ResourceRepository resourceRepository,
            AuthorizationService authorizationService
    ) {
        this.resourceRepository = resourceRepository;
        this.authorizationService = authorizationService;
    }

    /**
     * Create a child resource under a parent resource.
     */
    @Transactional
    public Resource createChildResource(
            UUID actorUserId,
            Resource parent,
            String resourceName,
            String resourceType
    ) {
        // 1️⃣ RBAC check
        boolean allowed = authorizationService.hasPermission(
                actorUserId,
                "CREATE_RESOURCE",
                parent
        );

        if (!allowed) {
            throw new SecurityException("Permission denied: CREATE_RESOURCE");
        }

        // 2️⃣ Create resource via factory
        Resource child = ResourceFactory.createChildResource(
                parent,
                resourceName,
                resourceType
        );

        // 3️⃣ Persist
        return resourceRepository.save(child);
    }
}
