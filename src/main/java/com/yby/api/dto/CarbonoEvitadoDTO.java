package com.yby.api.dto;

import com.yby.api.entity.enums.Bioma;
import java.math.BigDecimal;

/**
 * Saida da analise de Carbono Evitado (avoided emissions - nucleo do JREDD+).
 *
 * <p>Estima as emissoes evitadas pela reducao do desmatamento de um ano frente a linha de base
 * (media historica de desmatamento), convertendo os hectares evitados em tCO2e por um fator de
 * carbono por bioma, e o valor potencial em creditos de carbono.</p>
 *
 * @param municipioId               municipio avaliado
 * @param ano                       ano de referencia
 * @param bioma                     bioma usado para o fator de carbono
 * @param linhaBaseDesmatamentoHa   media historica anual de desmatamento (ha)
 * @param desmatamentoAnoHa         desmatamento do ano de referencia (ha)
 * @param hectaresEvitados          max(0, linhaBase - ano) em ha
 * @param fatorCarbonoTco2ePorHa    fator de carbono aplicado (tCO2e/ha)
 * @param tco2eEvitado              emissoes evitadas (tCO2e)
 * @param precoTonelada             preco por tonelada considerado (R$)
 * @param valorPotencialReais       valor potencial em creditos de carbono (R$)
 * @param versaoAlgoritmo           versao do algoritmo (RN-300)
 */
public record CarbonoEvitadoDTO(
    Long municipioId,
    Integer ano,
    Bioma bioma,
    BigDecimal linhaBaseDesmatamentoHa,
    BigDecimal desmatamentoAnoHa,
    BigDecimal hectaresEvitados,
    BigDecimal fatorCarbonoTco2ePorHa,
    BigDecimal tco2eEvitado,
    BigDecimal precoTonelada,
    BigDecimal valorPotencialReais,
    String versaoAlgoritmo
) {
}
