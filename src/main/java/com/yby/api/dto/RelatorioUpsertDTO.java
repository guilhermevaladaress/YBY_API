package com.yby.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RelatorioUpsertDTO(
    @NotNull Long municipioId,
    @NotBlank @Size(max = 180) String titulo,
    @NotBlank String resumo,
    String recomendacoes,
    @NotNull LocalDate dataReferencia
) {
}
