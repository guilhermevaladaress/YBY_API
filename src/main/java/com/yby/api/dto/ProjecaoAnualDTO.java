package com.yby.api.dto;

import java.math.BigDecimal;

/**
 * Um ano da projeção financeira de recebimento de crédito de carbono.
 *
 * <p>O preço por tonelada é armazenado em USD (padrão do mercado voluntário global).
 * A conversão para reais usa a cotação USD/BRL da data de referência informada pelo usuário.</p>
 *
 * @param ano                    ano projetado
 * @param toneladasProjetadas    tCO₂e esperadas no ano (meta × cumprimento)
 * @param precoToneladaAnoUsd    preço por tonelada no ano em USD (com crescimento aplicado)
 * @param precoToneladaAnoReais  preço por tonelada no ano em R$ (USD × cotação)
 * @param receitaProjetadaUsd    receita esperada no ano em USD
 * @param receitaProjetadaReais  receita esperada no ano em R$
 */
public record ProjecaoAnualDTO(
    Integer ano,
    BigDecimal toneladasProjetadas,
    BigDecimal precoToneladaAnoUsd,
    BigDecimal precoToneladaAnoReais,
    BigDecimal receitaProjetadaUsd,
    BigDecimal receitaProjetadaReais
) {
}
