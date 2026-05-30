package com.yby.api.dto;

import com.yby.api.entity.enums.UserRole;

public record AuthUserDTO(
    Long id,
    String nome,
    String email,
    UserRole role,
    boolean ativo,
    boolean trocaSenhaPrimeiroAcesso
) {
}
