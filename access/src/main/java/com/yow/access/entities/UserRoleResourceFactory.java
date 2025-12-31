package com.yow.access.entities;

public final class UserRoleResourceFactory {

    private UserRoleResourceFactory() {
    }

    public static UserRoleResource create(
            AppUser user,
            Role role,
            Resource resource
    ) {
        UserRoleResource urr = new UserRoleResource();

        UserRoleResourceId id = new UserRoleResourceId();
        id.setUserId(user.getId());
        id.setRoleId(role.getId());
        id.setResourceId(resource.getId());

        urr.setId(id);
        urr.setUser(user);
        urr.setRole(role);
        urr.setResource(resource);

        return urr;
    }
}
