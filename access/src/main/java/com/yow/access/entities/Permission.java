package com.yow.access.entities;

import jakarta.persistence.*;

/**
 * Entity representing a permission.
 *
 * A permission defines an atomic action that can be granted to a role.
 *
 * Author: Alan Tchapda
 * Date: 2025-12-30
 */
@Entity
@Table(name = "permission")
public class Permission {

    @Id
    @Column(name = "id", nullable = false)
    private Short id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    // JPA only
    protected Permission() {
    }

    // Getters & Setters

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

