package com.yby.api.dto;

import com.yby.api.entity.enums.Semaforo;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MunicipioDTO(
    Long id,
    String nome,
    String codigoIbge,
    BigDecimal areaHa,
    BigDecimal scorePrioridade,
    Semaforo semaforo,
    BigDecimal kpiRetorno,
    OffsetDateTime ultimaAtualizacao
) {
}
