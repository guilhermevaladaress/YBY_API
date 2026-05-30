package com.yby.api.dto;

import java.math.BigDecimal;

public record CarbonoHistoricoDTO(
    Long municipioId,
    String nome,
    String codigoIbge,
    BigDecimal emissaoTotal,
    BigDecimal mediaDiaria
) {
}
