package com.yby.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Redefinicao efetiva de senha a partir do token recebido no fluxo "esqueci minha senha". */
public record RedefinirSenhaRequestDTO(
    @NotBlank String token,
    @NotBlank @Size(min = 8, max = 120) String novaSenha
) {
}
