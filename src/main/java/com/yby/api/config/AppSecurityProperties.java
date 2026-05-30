package com.yby.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppSecurityProperties(
    Jwt jwt,
    Cors cors
) {

    public record Jwt(String secret, long expiresInSeconds) {
    }

    public record Cors(String allowedOrigins, boolean allowAllInDev) {
    }
}
