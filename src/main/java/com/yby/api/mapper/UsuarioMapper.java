package com.yby.api.mapper;

import com.yby.api.dto.UsuarioDTO;
import com.yby.api.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getRole().name(),
            usuario.isAtivo(),
            usuario.isPrimeiroAcessoTrocaSenha()
        );
    }
}
