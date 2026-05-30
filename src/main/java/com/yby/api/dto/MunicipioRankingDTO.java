package com.yby.api.dto;

import java.math.BigDecimal;

public record MunicipioRankingDTO(
    Long id,
    String nome,
    String codigoIbge,
    BigDecimal scorePrioridade,
    String semaforo,
    BigDecimal areaHa,
    BigDecimal kpiRetorno
) {
}
