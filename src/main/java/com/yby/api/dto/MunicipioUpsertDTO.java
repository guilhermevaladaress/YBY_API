package com.yby.api.dto;

import com.yby.api.entity.enums.Semaforo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record MunicipioUpsertDTO(
    @NotBlank @Size(max = 120) String nome,
    @NotBlank @Pattern(regexp = "\\d{7}") String codigoIbge,
    @PositiveOrZero BigDecimal areaHa,
    @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal scorePrioridade,
    Semaforo semaforo,
    String geojsonPolygon
) {
}
