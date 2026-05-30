package com.yby.api.dto;

public record LoginResponseDTO(
    String tokenType,
    String accessToken,
    long expiresIn,
    AuthUserDTO usuario
) {
}
