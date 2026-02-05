package com.yow.access.repositories;

import com.yow.access.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Short> {

    Optional<Role> findByName(String name);
    
    // Find roles that are either system-wide (tenantId is null) or specific to the given tenant
    java.util.List<Role> findByTenantIdOrTenantIdIsNull(java.util.UUID tenantId);
    
    // Check global uniqueness / specific tenant uniqueness
    Optional<Role> findByNameAndTenantId(String name, java.util.UUID tenantId);
    Optional<Role> findByNameAndTenantIdIsNull(String name);
}

