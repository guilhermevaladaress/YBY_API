package com.yby.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record IndicadorAnualDTO(
    Long id,
    @NotNull Long municipioId,
    @NotNull @Min(2000) @Max(2100) Integer ano,
    @PositiveOrZero BigDecimal gastoPublico,
    @PositiveOrZero BigDecimal resultadoAmbiental,
    @DecimalFactor BigDecimal desmatamentoRecenteFactor,
    @DecimalFactor BigDecimal eficienciaGastoFactor,
    @DecimalFactor BigDecimal irregularidadesCarFactor,
    @DecimalFactor BigDecimal areaElegivelFactor
) {
}
