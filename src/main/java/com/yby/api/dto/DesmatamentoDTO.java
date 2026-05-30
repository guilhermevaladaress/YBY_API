package com.yby.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DesmatamentoDTO(
    Long id,
    @NotNull Long municipioId,
    @NotBlank String fonte,
    @NotNull LocalDate dataReferencia,
    @PositiveOrZero BigDecimal areaHa,
    @NotBlank String bioma
) {
}
