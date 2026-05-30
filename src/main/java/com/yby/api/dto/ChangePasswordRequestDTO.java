package com.yby.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
    @NotBlank String senhaAtual,
    @NotBlank @Size(min = 8, max = 120) String novaSenha
) {
}
