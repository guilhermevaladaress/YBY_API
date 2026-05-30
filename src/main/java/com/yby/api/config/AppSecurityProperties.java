package com.yby.api.config;

import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppSecurityProperties(
    @DefaultValue Jwt jwt,
    @DefaultValue Cors cors
) {

    public record Jwt(
        @DefaultValue("dev-only-secret-key-change-me-dev-only-secret-key-change-me") String secret,
        @DefaultValue("28800") long expiresInSeconds
    ) {
    }

    public record Cors(
        @DefaultValue("http://localhost:3000,http://localhost:5173") String allowedOrigins,
        @DefaultValue("true") boolean allowAllInDev
    ) {
    }
}
