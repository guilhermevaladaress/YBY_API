package com.yby.api.dto;

import com.yby.api.entity.enums.SyncFonte;
import com.yby.api.entity.enums.SyncStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SyncJobDTO(
    UUID id,
    SyncFonte fonte,
    SyncStatus status,
    Integer registrosInseridos,
    String mensagemErro,
    OffsetDateTime iniciadoEm,
    OffsetDateTime finalizadoEm
) {
}
