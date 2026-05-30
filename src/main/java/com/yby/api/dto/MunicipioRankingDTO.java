package com.yby.api.dto;

import com.yby.api.entity.enums.Semaforo;
import java.math.BigDecimal;

public record MunicipioRankingDTO(
    Long id,
    String nome,
    String codigoIbge,
    BigDecimal scorePrioridade,
    Semaforo semaforo,
    BigDecimal areaHa,
    BigDecimal kpiRetorno
) {
}
