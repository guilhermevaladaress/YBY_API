package com.yby.api.dto;

import java.math.BigDecimal;

public record KpiDTO(
    Long municipioId,
    Integer anoInicio,
    Integer anoFim,
    BigDecimal gastoPublico,
    BigDecimal resultadoAmbiental,
    BigDecimal kpiRetorno
) {
}
