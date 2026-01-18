package com.yow.access.repositories;

import com.yow.access.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
        select distinct r.tenant
        from UserRoleResource urr
        join urr.resource r
        where urr.user.id = :userId
          and r.parent is null
    """)
    List<Tenant> findTenantsAccessibleByUser(UUID userId);
}
