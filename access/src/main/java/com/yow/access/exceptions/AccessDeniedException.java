package com.yow.access.exceptions;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String permission) {
        super("Access denied. Missing permission: " + permission);
    }
}
