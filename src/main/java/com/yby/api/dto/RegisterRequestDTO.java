package com.yby.api.dto;

import com.yby.api.entity.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
    @NotBlank @Size(max = 120) String nome,
    @Email @NotBlank String email,
    @NotBlank @Size(min = 8, max = 120) String senha,
    UserRole role
) {
}
