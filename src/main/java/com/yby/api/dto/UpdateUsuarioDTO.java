package com.yby.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Atualizacao de usuario pelo gestor (nome, perfil e, opcionalmente, redefinicao de senha).
 * Senha em branco/nula mantem a senha atual.
 */
public record UpdateUsuarioDTO(
    @NotBlank String nome,
    @NotBlank @Pattern(regexp = "^(GESTOR|SERVIDOR)$") String role,
    @Size(min = 8, max = 120) String senha
) {
}
