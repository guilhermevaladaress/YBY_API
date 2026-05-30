package com.yby.api.dto;

import java.math.BigDecimal;

public record EstatisticaGeralDTO(
    long totalMunicipios,
    long totalAlertasAtivos,
    BigDecimal totalAreaDesmatada12Meses,
    BigDecimal gastoPublicoTotal,
    BigDecimal resultadoAmbientalTotal,
    BigDecimal kpiMedioRetorno
) {
}
