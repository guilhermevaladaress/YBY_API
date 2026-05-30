package com.yby.api.dto;

import com.yby.api.entity.enums.EstrategiaAlocacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Entrada da RN-200 (Otimizacao de Alocacao de orcamento publico).
 *
 * @param orcamentoTotal          orcamento disponivel para distribuir (R$)
 * @param ano                     ano de referencia dos indicadores (opcional)
 * @param estrategia              maximo_kpi | equidade_regional | risco_minimo
 * @param maxMunicipios           limite de municipios contemplados (opcional)
 * @param valorMinimoPorMunicipio piso de alocacao por municipio para evitar pulverizacao (opcional)
 */
public record AlocacaoRequestDTO(
    @NotNull @Positive BigDecimal orcamentoTotal,
    Integer ano,
    @NotNull EstrategiaAlocacao estrategia,
    @PositiveOrZero Integer maxMunicipios,
    @PositiveOrZero BigDecimal valorMinimoPorMunicipio
) {
}
