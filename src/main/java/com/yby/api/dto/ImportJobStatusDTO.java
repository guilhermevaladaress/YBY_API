package com.yby.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ImportJobStatusDTO(
    Long jobId,
    String status,
    Integer registrosInseridos,
    List<String> erros,
    OffsetDateTime finalizadoEm
) {
}
