package com.yow.access.repositories;

import com.yow.access.entities.UserRoleResource;
import com.yow.access.entities.UserRoleResourceId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserRoleResourceRepository
        extends JpaRepository<UserRoleResource, UserRoleResourceId> {

    List<UserRoleResource> findAllByIdUserId(UUID userId);

    boolean existsByIdUserIdAndIdRoleIdAndIdResourceId(
            UUID userId,
            Short roleId,
            Long resourceId
    );

    List<UserRoleResource> findAllByUserId(UUID userId);
}

