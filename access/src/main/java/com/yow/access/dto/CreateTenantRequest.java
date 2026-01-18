package com.yow.access.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateTenantRequest {
    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Pattern(regexp = "^[A-Z0-9_]+$")
    private String code;

    public CreateTenantRequest() {
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
}

