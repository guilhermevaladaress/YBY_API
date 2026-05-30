package com.yby.api.dto;

public record UsuarioDTO(
    Long id,
    String nome,
    String email,
    String role,
    Boolean ativo,
    Boolean primeiroAcessoTrocaSenha
) {
}
