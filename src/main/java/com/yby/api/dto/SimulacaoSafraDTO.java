package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado da simulacao de financiamento do Plano Safra para uma area.
 *
 * @param areaHa             area simulada (ha)
 * @param itens              linhas elegiveis com valor financiavel e potencial de carbono
 * @param totalFinanciavel   maior valor financiavel entre as linhas (R$)
 * @param tco2ePotencialAno  reducao de CO2e estimada no ano para a melhor linha de carbono (tCO2e)
 * @param receitaCarbonoAno  receita anual de credito de carbono estimada (R$), se preco informado
 * @param versaoAlgoritmo    versao do algoritmo (RN-300)
 */
public record SimulacaoSafraDTO(
    BigDecimal areaHa,
    List<SimulacaoSafraItemDTO> itens,
    BigDecimal totalFinanciavel,
    BigDecimal tco2ePotencialAno,
    BigDecimal receitaCarbonoAno,
    String versaoAlgoritmo
) {

    /**
     * Uma linha elegivel da simulacao.
     *
     * @param linhaId            id da linha de credito
     * @param nome               nome da linha
     * @param programa           programa do Plano Safra
     * @param taxaJurosAa        taxa de juros a.a. (fracao)
     * @param valorFinanciavel   valor financiavel para a area (R$), limitado ao teto
     * @param custoJurosAnoUm    juros estimados no primeiro ano (R$)
     * @param tco2eAno           reducao de CO2e estimada no ano (tCO2e), se linha de carbono
     */
    public record SimulacaoSafraItemDTO(
        Long linhaId,
        String nome,
        String programa,
        BigDecimal taxaJurosAa,
        BigDecimal valorFinanciavel,
        BigDecimal custoJurosAnoUm,
        BigDecimal tco2eAno
    ) {
    }
}
