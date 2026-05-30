package com.yby.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUsuarioDTO(
    @NotBlank String nome,
    @NotBlank @Email String email,
    @NotBlank @Pattern(regexp = "^(GESTOR|SERVIDOR)$") String role
) {
}
