package com.yby.api.mapper;

import com.yby.api.dto.AuthUserDTO;
import com.yby.api.entity.UserAccount;

public final class AuthMapper {

    private AuthMapper() {
    }

    public static AuthUserDTO toDto(UserAccount user) {
        return new AuthUserDTO(
            user.getId(),
            user.getNome(),
            user.getEmail(),
            user.getRole(),
            user.isAtivo(),
            user.isTrocaSenhaPrimeiroAcesso()
        );
    }
}
