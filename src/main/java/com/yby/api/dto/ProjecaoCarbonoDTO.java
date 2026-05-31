package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Projeção financeira de recebimento de crédito rural de carbono ao longo dos anos.
 *
 * <p>Pode representar um único crédito ({@code creditoId} preenchido) ou a consolidação de
 * vários créditos para o dashboard ({@code creditoId} nulo).
 * Preços e receitas são apresentados em USD (moeda do mercado voluntário global) e em R$
 * (convertidos pela cotação da data de referência informada pelo usuário).</p>
 *
 * @param creditoId          id do crédito quando individual; nulo quando consolidado
 * @param municipioId        município quando filtrado; nulo quando consolidado geral
 * @param anoBase            ano base da projeção
 * @param itens              série anual projetada
 * @param tco2eTotal         soma das toneladas projetadas no horizonte
 * @param receitaTotalUsd    soma das receitas projetadas no horizonte em USD
 * @param receitaTotalReais  soma das receitas projetadas no horizonte em R$
 * @param cotacao            cotação USD/BRL usada na conversão
 * @param grafico            dados prontos para renderização de gráficos
 * @param versaoAlgoritmo    versão do algoritmo (RN-300)
 */
public record ProjecaoCarbonoDTO(
    Long creditoId,
    Long municipioId,
    Integer anoBase,
    List<ProjecaoAnualDTO> itens,
    BigDecimal tco2eTotal,
    BigDecimal receitaTotalUsd,
    BigDecimal receitaTotalReais,
    CotacaoDolarDTO cotacao,
    GraficoProjecaoDTO grafico,
    String versaoAlgoritmo
) {
}
