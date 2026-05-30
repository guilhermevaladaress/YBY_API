package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record DesmatamentoResumoDTO(
    Integer ano,
    BigDecimal totalAreaHa,
    List<DesmatamentoResumoItemDTO> breakdown
) {
}
