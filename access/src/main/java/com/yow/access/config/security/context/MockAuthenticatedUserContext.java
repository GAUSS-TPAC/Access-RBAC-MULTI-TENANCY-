package com.yow.access.config.security.context;


import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Temporary authenticated user context for development.
 */
@Component
@Profile("dev")
public class MockAuthenticatedUserContext implements AuthenticatedUserContext {

    @Override
    public UUID getUserId() {
        // Temporary fixed user for bootstrap/testing
        return UUID.fromString("00000000-0000-0000-0000-000000000001");
    }
}
