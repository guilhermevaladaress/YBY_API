package com.yby.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CarbonoRegistroDTO(
    Long id,
    @NotNull Long municipioId,
    @NotNull LocalDate dataReferencia,
    @NotNull @PositiveOrZero BigDecimal emissaoTco2e,
    @NotBlank String fonte,
    String observacao
) {
}
