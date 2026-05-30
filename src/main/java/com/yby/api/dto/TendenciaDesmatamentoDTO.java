package com.yby.api.dto;

import com.yby.api.entity.enums.StatusTendencia;
import java.math.BigDecimal;

/**
 * Saida da RN-101-A (Score de Prioridade com Tendencia).
 *
 * @param municipioId               municipio avaliado
 * @param ano                       ano de referencia
 * @param desmatamentoAno           area desmatada (ha) no ano
 * @param desmatamentoAnoAnterior   area desmatada (ha) no ano anterior
 * @param tendenciaDesmatamento     variacao percentual ano/ano (ex.: 12.50 = +12,5%)
 * @param statusTendencia           ACELERANDO | ESTAVEL | MELHORANDO
 * @param ajusteScore               pontos somados/subtraidos ao score base (+15 / -10 / 0)
 * @param versaoAlgoritmo           versao do algoritmo (RN-300)
 */
public record TendenciaDesmatamentoDTO(
    Long municipioId,
    Integer ano,
    BigDecimal desmatamentoAno,
    BigDecimal desmatamentoAnoAnterior,
    BigDecimal tendenciaDesmatamento,
    StatusTendencia statusTendencia,
    int ajusteScore,
    String versaoAlgoritmo
) {
}
