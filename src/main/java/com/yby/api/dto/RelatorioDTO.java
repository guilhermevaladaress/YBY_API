package com.yby.api.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record RelatorioDTO(
    Long id,
    Long municipioId,
    String municipioNome,
    String titulo,
    String resumo,
    String recomendacoes,
    LocalDate dataReferencia,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
