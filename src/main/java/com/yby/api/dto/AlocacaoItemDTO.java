package com.yby.api.dto;

import java.math.BigDecimal;

/**
 * Item de alocacao de orcamento para um municipio (RN-200).
 *
 * @param municipioId            municipio contemplado
 * @param nome                   nome do municipio
 * @param scorePrioridade        score de prioridade considerado
 * @param valorAlocado           valor alocado (R$)
 * @param retornoEsperado        retorno ambiental esperado (valor x KPI de retorno)
 * @param impactoSocialEstimado  beneficiarios sociais sustentados/ampliados (empregos + familias PSA)
 * @param justificativa          justificativa auditavel da alocacao
 */
public record AlocacaoItemDTO(
    Long municipioId,
    String nome,
    BigDecimal scorePrioridade,
    BigDecimal valorAlocado,
    BigDecimal retornoEsperado,
    BigDecimal impactoSocialEstimado,
    String justificativa
) {
}
