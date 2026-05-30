package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record EstatisticaDashboardDTO(
    long totalMunicipios,
    long totalAlertasAtivos,
    long totalJobsSync,
    BigDecimal scoreMedioPrioridade,
    List<MunicipioRankingDTO> ranking
) {
}
