package com.yby.api.dto;

import com.yby.api.entity.enums.CreditoStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Cadastro/saida de credito rural de carbono (meta de tCO2e/ano por municipio).
 */
public record CreditoCarbonoDTO(
    Long id,
    @NotNull Long municipioId,
    @NotNull @Min(2000) @Max(2100) Integer anoBase,
    @NotNull @PositiveOrZero BigDecimal metaTco2eAno,
    @NotNull @PositiveOrZero BigDecimal precoTonelada,
    @PositiveOrZero BigDecimal taxaCrescimentoPreco,
    @PositiveOrZero BigDecimal percentualCumprimentoMeta,
    @NotNull @Min(1) @Max(50) Integer horizonteAnos,
    String descricao,
    CreditoStatus status
) {
}
