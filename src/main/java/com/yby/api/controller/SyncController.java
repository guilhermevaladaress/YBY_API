package com.yby.api.controller;

import com.yby.api.dto.SyncJobDTO;
import com.yby.api.dto.SyncStatusUpdateDTO;
import com.yby.api.entity.enums.SyncFonte;
import com.yby.api.service.SyncService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/sync", "/api/sync"})
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/deter")
    public ResponseEntity<SyncJobDTO> syncDeter() {
        return ResponseEntity.accepted().body(syncService.iniciar(SyncFonte.DETER));
    }

    @PostMapping("/prodes")
    public ResponseEntity<SyncJobDTO> syncProdes() {
        return ResponseEntity.accepted().body(syncService.iniciar(SyncFonte.PRODES));
    }

    @PostMapping("/ibge")
    public ResponseEntity<SyncJobDTO> syncIbge() {
        return ResponseEntity.accepted().body(syncService.iniciar(SyncFonte.IBGE));
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<SyncJobDTO>> listarJobs(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(syncService.listar(pageable));
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<SyncJobDTO> buscarJob(@PathVariable UUID id) {
        return ResponseEntity.ok(syncService.buscar(id));
    }

    @PatchMapping("/jobs/{id}")
    public ResponseEntity<SyncJobDTO> atualizarStatus(@PathVariable UUID id, @Valid @RequestBody SyncStatusUpdateDTO dto) {
        return ResponseEntity.ok(syncService.atualizarStatus(id, dto));
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deletarJob(@PathVariable UUID id) {
        syncService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
