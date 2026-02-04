package com.yow.access.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class RoleAssignmentRequest {

    @NotNull(message = "User ID is required")
    private UUID targetUserId;

    @NotNull(message = "Role ID is required")
    private Short roleId;

    @NotNull(message = "Resource ID is required")
    private UUID resourceId;

    public RoleAssignmentRequest() {}

    public UUID getTargetUserId() { return targetUserId; }
    public void setTargetUserId(UUID targetUserId) { this.targetUserId = targetUserId; }
    public Short getRoleId() { return roleId; }
    public void setRoleId(Short roleId) { this.roleId = roleId; }
    public UUID getResourceId() { return resourceId; }
    public void setResourceId(UUID resourceId) { this.resourceId = resourceId; }
}
