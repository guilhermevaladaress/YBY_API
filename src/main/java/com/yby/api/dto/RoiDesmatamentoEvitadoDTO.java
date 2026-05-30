package com.yby.api.dto;

import java.math.BigDecimal;

/**
 * Saida da analise financeira de ROI de Desmatamento Evitado.
 *
 * <p>Compara a receita JREDD+ de manter a floresta (carbono evitado x preco) com o ganho
 * economico de converter a area (ex.: agropecuaria, R$/ha/ano), retornando o ROI e uma
 * recomendacao acionavel.</p>
 *
 * @param municipioId               municipio avaliado
 * @param ano                       ano de referencia
 * @param hectaresEvitados          hectares de desmatamento evitado considerados
 * @param tco2eEvitado              emissoes evitadas (tCO2e)
 * @param precoTonelada             preco por tonelada de CO2e (R$)
 * @param receitaJreddReais         receita JREDD+ de manter a floresta (R$)
 * @param valorAgropecuariaHaAno    valor de conversao por hectare/ano (R$/ha/ano)
 * @param ganhoConversaoReais       ganho economico estimado da conversao (R$)
 * @param roi                       receitaJredd / ganhoConversao
 * @param recomendacao              recomendacao textual
 * @param versaoAlgoritmo           versao do algoritmo (RN-300)
 */
public record RoiDesmatamentoEvitadoDTO(
    Long municipioId,
    Integer ano,
    BigDecimal hectaresEvitados,
    BigDecimal tco2eEvitado,
    BigDecimal precoTonelada,
    BigDecimal receitaJreddReais,
    BigDecimal valorAgropecuariaHaAno,
    BigDecimal ganhoConversaoReais,
    BigDecimal roi,
    String recomendacao,
    String versaoAlgoritmo
) {
}
