package com.yow.access.exceptions;

public class TenantAlreadyExistsException extends RuntimeException {

    public TenantAlreadyExistsException(String code) {
        super("Tenant with code '" + code + "' already exists");
    }
}
