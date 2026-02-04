package com.yow.access.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_role_resource")
public class UserRoleResource {

    @EmbeddedId
    private UserRoleResourceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("resourceId")
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();

    public UserRoleResource() {}

    public UserRoleResourceId getId() { return id; }
    public void setId(UserRoleResourceId id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }
    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }
}
