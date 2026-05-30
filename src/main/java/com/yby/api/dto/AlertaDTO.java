package com.yby.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AlertaDTO(
    Long id,
    @NotNull Long municipioId,
    @NotBlank String tipo,
    @NotBlank String gravidade,
    @NotBlank String descricao,
    @NotBlank String acaoRecomendada,
    LocalDate dataAlerta,
    Boolean ativo
) {
}
