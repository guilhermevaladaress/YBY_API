package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDTO(
    long totalMunicipios,
    long municipiosCriticos,
    BigDecimal kpiMedioRetorno,
    List<MunicipioRankingDTO> top5Prioridade
) {
}
