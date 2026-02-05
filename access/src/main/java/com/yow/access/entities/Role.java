package com.yow.access.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @Column(name = "id", nullable = false)
    private Short id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "scope", nullable = false, length = 20)
    private String scope;
    
    @Column(name = "tenant_id")
    private java.util.UUID tenantId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    public Role() {}

    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public java.util.UUID getTenantId() { return tenantId; }
    public void setTenantId(java.util.UUID tenantId) { this.tenantId = tenantId; }
    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
}
