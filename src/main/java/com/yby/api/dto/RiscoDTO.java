package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record RiscoDTO(
    Long municipioId,
    BigDecimal notaRisco,
    String semaforo,
    List<String> pendencias
) {
}
