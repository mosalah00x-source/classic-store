package com.classicstore.identity.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {

    public static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 900; // 15 minutes
    public static final long REFRESH_TOKEN_EXPIRATION_SECONDS = 7 * 24 * 3600; // 7 days

    private final String secret;

    public JwtProperties(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("jwt.secret property must be configured");
        }
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }

    public long getAccessTokenExpirationSeconds() {
        return ACCESS_TOKEN_EXPIRATION_SECONDS;
    }

    public long getRefreshTokenExpirationSeconds() {
        return REFRESH_TOKEN_EXPIRATION_SECONDS;
    }
}
