package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dados de projeção formatados para renderização de gráficos (compatível com Chart.js).
 *
 * <p>Cada série contém um rótulo e uma lista de valores indexados pelos anos em {@code labels}.
 * O frontend pode usar diretamente no datasets de um gráfico de barras ou linha.</p>
 *
 * @param labels              anos projetados (eixo X)
 * @param receitaReais        série de receita anual em R$
 * @param receitaUsd          série de receita anual em USD
 * @param tco2e               série de toneladas de CO2e projetadas por ano
 * @param precoToneladaUsd    série do preço por tonelada em USD (com crescimento)
 * @param precoToneladaReais  série do preço por tonelada em R$ (convertido pela cotação)
 */
public record GraficoProjecaoDTO(
    List<Integer> labels,
    SerieDTO receitaReais,
    SerieDTO receitaUsd,
    SerieDTO tco2e,
    SerieDTO precoToneladaUsd,
    SerieDTO precoToneladaReais
) {

    /**
     * Uma série de dados para um gráfico.
     *
     * @param label  rótulo da série (legenda)
     * @param data   valores correspondentes a cada item de {@code labels}
     */
    public record SerieDTO(String label, List<BigDecimal> data) {
    }
}
