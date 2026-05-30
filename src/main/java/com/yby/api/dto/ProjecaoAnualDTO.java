package com.yby.api.dto;

import java.math.BigDecimal;

/**
 * Um ano da projecao financeira de recebimento de credito de carbono.
 *
 * @param ano                   ano projetado
 * @param toneladasProjetadas   tCO2e esperadas no ano (meta x cumprimento)
 * @param precoToneladaAno      preco por tonelada no ano (com crescimento aplicado)
 * @param receitaProjetadaReais receita esperada no ano (R$)
 */
public record ProjecaoAnualDTO(
    Integer ano,
    BigDecimal toneladasProjetadas,
    BigDecimal precoToneladaAno,
    BigDecimal receitaProjetadaReais
) {
}
