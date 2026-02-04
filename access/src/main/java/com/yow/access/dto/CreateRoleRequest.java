package com.yow.access.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateRoleRequest {

    @NotBlank(message = "Role name is required")
    private String name;

    private String scope; // "GLOBAL" or "TENANT"

    private List<Short> permissionIds;

    public CreateRoleRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public List<Short> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(List<Short> permissionIds) { this.permissionIds = permissionIds; }
}
