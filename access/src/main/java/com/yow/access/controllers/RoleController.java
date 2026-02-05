package com.yow.access.controllers;

import com.yow.access.dto.CreateRoleRequest;
import com.yow.access.entities.Permission;
import com.yow.access.entities.Role;
import com.yow.access.repositories.PermissionRepository;
import com.yow.access.repositories.RoleRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final com.yow.access.config.security.context.AuthenticatedUserContext userContext;

    public RoleController(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            com.yow.access.config.security.context.AuthenticatedUserContext userContext
    ) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles(@RequestParam(required = false) java.util.UUID tenantId) {
        // If tenantId is provided, return System roles + Tenant roles
        // If not provided, return ALL roles (SupAdmin) or just System roles?
        // Let's assume:
        // - if tenantId param present -> return specialized view
        // - if absent -> return all (legacy behavior for SupAdmin dashboard)
        
        // Better: if absent, return ALL.
        
        List<Role> roles;
        if (tenantId != null) {
            roles = roleRepository.findByTenantIdOrTenantIdIsNull(tenantId);
        } else {
            roles = roleRepository.findAll();
        }
        
        return ResponseEntity.ok(roles);
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@Valid @RequestBody CreateRoleRequest request) {
        // Validation uniqueness based on scope
        if (request.getTenantId() != null) {
            // Tenant Role
            if (roleRepository.findByNameAndTenantId(request.getName(), request.getTenantId()).isPresent()) {
                 throw new IllegalArgumentException("Un rôle personnalisé avec ce nom existe déjà pour cette organisation.");
            }
        } else {
            // System Role
            if (roleRepository.findByNameAndTenantIdIsNull(request.getName()).isPresent()) {
                throw new IllegalArgumentException("Un rôle système avec ce nom existe déjà.");
            }
            // Also check if it conflicts with a global unique constraint if any (old logic)
            // But we dropped unique constraint. However, good to keep names distinct generally if possible?
            // Actually, if I create "MANAGER" as system role, and Tenant A has "MANAGER" custom role, it should be fine?
            // Yes, user sees both? Or user sees custom overriding system?
            // Usually System roles are strictly reserved.
            // Let's keep it simple: check exact match for now.
        }

        // Trouver le prochain ID disponible
        Short nextId = (short) (roleRepository.findAll().stream()
                .mapToInt(Role::getId)
                .max()
                .orElse(0) + 1);

        // Créer le rôle
        Role role = new Role();
        role.setId(nextId);
        role.setName(request.getName());
        role.setScope(request.getScope() != null ? request.getScope() : "TENANT");
        role.setTenantId(request.getTenantId());

        // Assigner les permissions si fournies
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            for (Short permId : request.getPermissionIds()) {
                Permission perm = permissionRepository.findById(permId)
                        .orElseThrow(() -> new IllegalArgumentException("Permission introuvable: " + permId));
                permissions.add(perm);
            }
            role.setPermissions(permissions);
        }

        roleRepository.save(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }
}
