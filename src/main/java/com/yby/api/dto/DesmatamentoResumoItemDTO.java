package com.yby.api.dto;

import java.math.BigDecimal;

public record DesmatamentoResumoItemDTO(
    String bioma,
    String semaforo,
    BigDecimal areaTotalHa
) {
}
