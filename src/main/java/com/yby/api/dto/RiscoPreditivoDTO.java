package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Saida da RN-108-A (Risco Preditivo com Historico).
 *
 * @param municipioId      municipio avaliado
 * @param notaBase         composicao 0-10 (embargos IBAMA, sobreposicao TI/UC, CAR irregular)
 * @param fatorTempo       multiplicador temporal aplicado (embargo recente, ausencia, plano ativo)
 * @param notaRiscoFinal   nota final ja limitada a 0-10
 * @param semaforoRisco    verde | amarelo | vermelho derivado da nota
 * @param embargoRecente   true se ha embargo nos ultimos 6 meses
 * @param planoAcaoAtivo   true se ha plano de acao ambiental ativo
 * @param pendencias       lista acionavel de pendencias (RN-109)
 * @param versaoAlgoritmo  versao do algoritmo (RN-300)
 */
public record RiscoPreditivoDTO(
    Long municipioId,
    BigDecimal notaBase,
    BigDecimal fatorTempo,
    BigDecimal notaRiscoFinal,
    String semaforoRisco,
    boolean embargoRecente,
    boolean planoAcaoAtivo,
    List<String> pendencias,
    String versaoAlgoritmo
) {
}
