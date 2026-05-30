package com.yby.api.dto;

import com.yby.api.entity.enums.ProgramaSafra;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Cadastro/saida de uma linha de credito rural do Plano Safra.
 */
public record LinhaCreditoSafraDTO(
    Long id,
    @NotBlank String nome,
    @NotNull ProgramaSafra programa,
    @NotBlank @Pattern(regexp = "\\d{4}/\\d{4}", message = "ano-safra deve estar no formato AAAA/AAAA")
    String anoSafra,
    String instituicaoFinanceira,
    @NotNull @PositiveOrZero BigDecimal taxaJurosAa,
    @PositiveOrZero BigDecimal tetoFinanciamento,
    Integer prazoMeses,
    Integer carenciaMeses,
    boolean exigePraticaCarbono,
    @PositiveOrZero BigDecimal fatorReducaoTco2eHa,
    String descricao,
    Boolean ativo
) {
}
