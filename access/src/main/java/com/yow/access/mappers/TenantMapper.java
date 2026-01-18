package com.yow.access.mappers;

import com.yow.access.dto.TenantResponse;
import com.yow.access.entities.Tenant;

public final class TenantMapper {

    private TenantMapper() {}

    public static TenantResponse toResponse(Tenant tenant) {
        TenantResponse dto = new TenantResponse();
        dto.setId(tenant.getId());
        dto.setName(tenant.getName());
        dto.setCode(tenant.getCode());
        dto.setStatus(tenant.getStatus());
        dto.setCreatedAt(tenant.getCreatedAt());
        return dto;
    }
}

