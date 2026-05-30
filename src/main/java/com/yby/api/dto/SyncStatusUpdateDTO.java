package com.yby.api.dto;

import com.yby.api.entity.enums.SyncStatus;
import jakarta.validation.constraints.NotNull;

public record SyncStatusUpdateDTO(
    @NotNull SyncStatus status,
    Integer registrosInseridos,
    String mensagemErro
) {
}
