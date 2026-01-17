package com.yow.access.dto;

import com.yow.access.entities.Tenant;

import java.time.Instant;
import java.util.UUID;

public class TenantResponse {

    private UUID id;
    private String name;
    private String code;
    private String status;
    private Instant createdAt;

    public TenantResponse() {
    }

    /* ===== FACTORY METHOD ===== */
    public static TenantResponse fromEntity(Tenant tenant) {
        TenantResponse dto = new TenantResponse();
        dto.setId(tenant.getId());
        dto.setName(tenant.getName());
        dto.setCode(tenant.getCode());
        dto.setStatus(tenant.getStatus());
        dto.setCreatedAt(tenant.getCreatedAt());
        return dto;
    }

    /* ===== GETTERS / SETTERS ===== */

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
