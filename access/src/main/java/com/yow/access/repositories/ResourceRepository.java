package com.yow.access.repositories;

import com.yow.access.entities.Resource;
import com.yow.access.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findAllByTenant(Tenant tenant);

    List<Resource> findAllByParent(Resource parent);

    boolean existsByTenantAndPath(Tenant tenant, String path);
}

