package com.yby.api.mapper;

import com.yby.api.dto.SyncJobDTO;
import com.yby.api.entity.SyncJob;

public final class SyncMapper {

    private SyncMapper() {
    }

    public static SyncJobDTO toDto(SyncJob syncJob) {
        return new SyncJobDTO(
            syncJob.getId(),
            syncJob.getFonte(),
            syncJob.getStatus(),
            syncJob.getRegistrosInseridos(),
            syncJob.getMensagemErro(),
            syncJob.getIniciadoEm(),
            syncJob.getFinalizadoEm()
        );
    }
}
