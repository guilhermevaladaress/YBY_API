package com.yby.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record MunicipioDetalheDTO(
    Long id,
    String nome,
    String codigoIbge,
    BigDecimal scorePrioridade,
    String semaforo,
    BigDecimal notaRisco,
    BigDecimal kpiRetorno,
    BigDecimal areaHa,
    List<String> pendencias,
    OffsetDateTime ultimaAtualizacao
) {
}
