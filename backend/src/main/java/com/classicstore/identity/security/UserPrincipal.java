package com.classicstore.identity.security;

import java.util.UUID;

public class UserPrincipal {

    private final UUID userId;
    private final String role;

    public UserPrincipal(UUID userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}
