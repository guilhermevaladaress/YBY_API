package com.yby.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record MunicipioUpdateDTO(
    @Size(min = 1, max = 180) String nome,
    @Pattern(regexp = "^[0-9]{7}$") String codigoIbge,
    @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal scorePrioridade,
    @Pattern(regexp = "^(verde|amarelo|vermelho|VERDE|AMARELO|VERMELHO)$") String semaforo,
    @DecimalMin("0.0") BigDecimal areaHa,
    @DecimalMin("0.0") @DecimalMax("10.0") BigDecimal notaRisco,
    String pendenciasResumo,
    String geojsonPolygon
) {
}
