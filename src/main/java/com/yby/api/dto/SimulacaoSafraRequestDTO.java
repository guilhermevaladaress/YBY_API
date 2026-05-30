package com.yby.api.dto;

import com.yby.api.entity.enums.ProgramaSafra;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Entrada da simulacao de financiamento do Plano Safra para uma area rural.
 *
 * @param areaHa            area a ser financiada (hectares)
 * @param programa          filtra por programa (opcional)
 * @param somenteCarbono    quando verdadeiro, considera apenas linhas de baixa emissao
 * @param precoTonelada     preco de referencia do credito (R$/tCO2e) para estimar a receita de carbono (opcional)
 */
public record SimulacaoSafraRequestDTO(
    @NotNull @Positive BigDecimal areaHa,
    ProgramaSafra programa,
    boolean somenteCarbono,
    BigDecimal precoTonelada
) {
}
