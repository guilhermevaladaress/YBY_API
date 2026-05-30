package com.yby.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Solicitacao de redefinicao de senha (fluxo "esqueci minha senha"). */
public record EsqueciSenhaRequestDTO(
    @NotBlank @Email String email
) {
}
