package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Saida da analise de Projecao de Desmatamento.
 *
 * <p>Projeta a area desmatada (ha) dos proximos anos a partir da tendencia historica (taxa media
 * de crescimento composta), apoiando a definicao da linha de base do JREDD+.</p>
 *
 * @param municipioId        municipio avaliado
 * @param anoBase            ultimo ano com dado historico
 * @param horizonteAnos      anos projetados a frente
 * @param taxaMediaAnual     taxa media de crescimento anual (%) estimada
 * @param mediaHistoricaHa   media anual historica de desmatamento (ha)
 * @param serieHistorica     serie historica observada (ha por ano)
 * @param projecao           serie projetada (ha por ano)
 * @param versaoAlgoritmo    versao do algoritmo (RN-300)
 */
public record ProjecaoDesmatamentoDTO(
    Long municipioId,
    Integer anoBase,
    Integer horizonteAnos,
    BigDecimal taxaMediaAnual,
    BigDecimal mediaHistoricaHa,
    List<ValorAnualDTO> serieHistorica,
    List<ValorAnualDTO> projecao,
    String versaoAlgoritmo
) {
}
