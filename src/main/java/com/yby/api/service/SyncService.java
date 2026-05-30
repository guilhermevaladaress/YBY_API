package com.yby.api.service;

import com.yby.api.dto.SyncJobDTO;
import com.yby.api.dto.SyncStatusUpdateDTO;
import com.yby.api.entity.SyncJob;
import com.yby.api.entity.enums.SyncFonte;
import com.yby.api.entity.enums.SyncStatus;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.mapper.SyncMapper;
import com.yby.api.repository.SyncJobRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SyncService {

    private final SyncJobRepository syncJobRepository;
    private final AuditService auditService;

    public SyncService(SyncJobRepository syncJobRepository, AuditService auditService) {
        this.syncJobRepository = syncJobRepository;
        this.auditService = auditService;
    }

    @Transactional
    public SyncJobDTO iniciar(SyncFonte fonte) {
        SyncJob job = new SyncJob();
        job.setFonte(fonte);
        job.setStatus(SyncStatus.PROCESSANDO);
        job.setRegistrosInseridos(0);
        SyncJob saved = syncJobRepository.save(job);

        auditService.registrarEscritaGestor("SYNC_START", "sync_jobs", saved.getId().toString(), fonte.name());
        return SyncMapper.toDto(saved);
    }

    public Page<SyncJobDTO> listar(Pageable pageable) {
        return syncJobRepository.findAll(pageable).map(SyncMapper::toDto);
    }

    public SyncJobDTO buscar(UUID id) {
        return SyncMapper.toDto(buscarEntidade(id));
    }

    @Transactional
    public SyncJobDTO atualizarStatus(UUID id, SyncStatusUpdateDTO dto) {
        SyncJob job = buscarEntidade(id);
        job.setStatus(dto.status());
        if (dto.registrosInseridos() != null) {
            job.setRegistrosInseridos(dto.registrosInseridos());
        }
        if (dto.mensagemErro() != null) {
            job.setMensagemErro(dto.mensagemErro());
        }
        if (dto.status() != SyncStatus.PROCESSANDO) {
            job.setFinalizadoEm(OffsetDateTime.now());
        }
        SyncJob saved = syncJobRepository.save(job);
        auditService.registrarEscritaGestor("SYNC_UPDATE", "sync_jobs", saved.getId().toString(), dto);
        return SyncMapper.toDto(saved);
    }

    @Transactional
    public void deletar(UUID id) {
        SyncJob job = buscarEntidade(id);
        syncJobRepository.delete(job);
        auditService.registrarEscritaGestor("SYNC_DELETE", "sync_jobs", id.toString(), null);
    }

    public long totalJobs() {
        return syncJobRepository.count();
    }

    private SyncJob buscarEntidade(UUID id) {
        return syncJobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job de sync nao encontrado"));
    }
}
