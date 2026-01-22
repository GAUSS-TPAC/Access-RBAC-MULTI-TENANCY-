package com.yow.access.services;

import com.yow.access.entities.*;
import com.yow.access.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service responsible for user lifecycle and RBAC assignments.
 *
 * Author: Alan Tchapda
 * Date: 2025-12-30
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ResourceRepository resourceRepository;
    private final UserRoleResourceRepository urrRepository;
    private final AuthorizationService authorizationService;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            ResourceRepository resourceRepository,
            UserRoleResourceRepository urrRepository,
            AuthorizationService authorizationService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.resourceRepository = resourceRepository;
        this.urrRepository = urrRepository;
        this.authorizationService = authorizationService;
    }

    /**
     * Create a new user.
     */
    @Transactional
    public AppUser createUser(
            String username,
            String email,
            String passwordHash
    ) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /**
     * Enable or disable a user.
     */
    @Transactional
    public void setUserEnabled(UUID userId, boolean enabled) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        user.setEnabled(enabled);
    }

    /**
     * Assign a role to a user on a resource.
     */
    @Transactional
    public void assignRole(
            UUID actorUserId,
            UUID targetUserId,
            Short roleId,
            UUID resourceId
    ) {
        // 1️⃣ RBAC check
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalStateException("Resource not found"));

        boolean allowed = authorizationService.hasPermission(
                actorUserId,
                "ASSIGN_ROLE",
                resource
        );

        if (!allowed) {
            throw new SecurityException("Permission denied: ASSIGN_ROLE");
        }

        // 2️⃣ Load entities
        AppUser targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalStateException("Target user not found"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalStateException("Role not found"));

        // 3️⃣ SINGLE source of truth
        UserRoleResource urr =
                UserRoleResourceFactory.create(
                        targetUser,
                        role,
                        resource
                );

        urrRepository.save(urr);
    }
}