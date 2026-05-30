package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Projecao financeira de recebimento de credito rural de carbono ao longo dos anos.
 *
 * <p>Pode representar um unico credito ({@code creditoId} preenchido) ou a consolidacao de
 * varios creditos para o dashboard ({@code creditoId} nulo).</p>
 *
 * @param creditoId          id do credito quando individual; nulo quando consolidado
 * @param municipioId        municipio quando filtrado; nulo quando consolidado geral
 * @param anoBase            ano base da projecao
 * @param itens              serie anual projetada
 * @param tco2eTotal         soma das toneladas projetadas no horizonte
 * @param receitaTotalReais  soma das receitas projetadas no horizonte (R$)
 * @param versaoAlgoritmo    versao do algoritmo (RN-300)
 */
public record ProjecaoCarbonoDTO(
    Long creditoId,
    Long municipioId,
    Integer anoBase,
    List<ProjecaoAnualDTO> itens,
    BigDecimal tco2eTotal,
    BigDecimal receitaTotalReais,
    String versaoAlgoritmo
) {
}
