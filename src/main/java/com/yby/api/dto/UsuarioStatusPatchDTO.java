package com.yby.api.dto;

import jakarta.validation.constraints.NotNull;

public record UsuarioStatusPatchDTO(@NotNull Boolean ativo) {
}
