package com.yby.api.dto;

import com.yby.api.entity.enums.Semaforo;
import java.math.BigDecimal;
import java.util.List;

public record RiscoDTO(
    Long municipioId,
    BigDecimal notaRisco,
    Semaforo semaforo,
    List<String> pendencias
) {
}
