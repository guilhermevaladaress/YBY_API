package com.yby.api.dto;

import java.math.BigDecimal;

public record RetornoDTO(
    Long municipioId,
    BigDecimal gastoPublico,
    BigDecimal resultadoAmbiental,
    BigDecimal kpiRetorno,
    String classificacao
) {
}
