package com.yow.access.services;

import com.yow.access.entities.*;
import com.yow.access.entities.Resource;
import com.yow.access.entities.Role;
import com.yow.access.entities.UserRoleResource;
import com.yow.access.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service responsible for tenant bootstrap.
 *
 * Author: Alan Tchapda
 * Date: 2025-12-30
 */
@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleResourceRepository userRoleResourceRepository;

    public TenantService(
            TenantRepository tenantRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleResourceRepository userRoleResourceRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleResourceRepository = userRoleResourceRepository;
    }

    /**
     * Bootstrap a tenant with its root resource and initial TENANT_ADMIN.
     */
    @Transactional
    public void createTenant(String tenantName, String tenantCode, UUID creatorUserId) {

        // 1️⃣ Load creator
        AppUser creator = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new IllegalStateException("Creator user not found"));

        // 2️⃣ Create tenant
        Tenant tenant = new Tenant();
        tenant.setName(tenantName);
        tenant.setCode(tenantCode);
        tenant.setStatus("ACTIVE");

        tenantRepository.save(tenant);

        // 3️⃣ Create root resource via factory (1 tenant = 1 root resource)
        Resource rootResource =
                ResourceFactory.createRootResource(tenant, tenantName);

        resourceRepository.save(rootResource);

        // 4️⃣ Load TENANT_ADMIN role
        Role tenantAdminRole = roleRepository.findByName("TENANT_ADMIN")
                .orElseThrow(() -> new IllegalStateException("TENANT_ADMIN role not found"));

        // 5️⃣ Assign role via factory
        UserRoleResource urr =
                UserRoleResourceFactory.create(creator, tenantAdminRole, rootResource);

        userRoleResourceRepository.save(urr);
    }
}
