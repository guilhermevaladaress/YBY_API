package com.yby.api.dto;

import java.time.OffsetDateTime;

/**
 * Resumo de uma execucao do recalculo de inteligencia preditiva.
 *
 * @param municipiosProcessados   municipios com snapshot atualizado
 * @param anoBase                 ultimo ano com desmatamento (ancora dos calculos)
 * @param focosEstadoAnoCorrente  total de focos de calor INPE (TO) no ano corrente
 * @param versaoAlgoritmo         versao do algoritmo (RN-300)
 * @param executadoEm             timestamp ISO-8601 da execucao
 */
public record RecalculoInteligenciaDTO(
    int municipiosProcessados,
    Integer anoBase,
    long focosEstadoAnoCorrente,
    String versaoAlgoritmo,
    OffsetDateTime executadoEm
) {
}
