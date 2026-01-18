package com.yow.access.repositories;

import com.yow.access.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Short> {

    Optional<Permission> findByName(String name);
}

