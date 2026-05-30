package com.yby.api.dto;

import java.time.OffsetDateTime;

public record RelatorioDTO(
    String titulo,
    String tipo,
    OffsetDateTime geradoEm,
    String observacao
) {
}
