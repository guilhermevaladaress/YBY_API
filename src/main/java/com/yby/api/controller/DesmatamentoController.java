package com.yby.api.controller;

import com.yby.api.dto.DesmatamentoDTO;
import com.yby.api.dto.DesmatamentoResumoDTO;
import com.yby.api.dto.ImportJobStatusDTO;
import com.yby.api.service.DesmatamentoService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/desmatamento")
public class DesmatamentoController {

    private final DesmatamentoService desmatamentoService;

    public DesmatamentoController(DesmatamentoService desmatamentoService) {
        this.desmatamentoService = desmatamentoService;
    }

    @GetMapping("/{municipioId}/historico")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<List<DesmatamentoDTO>> historico(
        @PathVariable Long municipioId,
        @RequestParam(defaultValue = "ALL") String fonte,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return ResponseEntity.ok(desmatamentoService.historico(municipioId, fonte, dataInicio, dataFim));
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<DesmatamentoResumoDTO> resumo(@RequestParam Integer ano) {
        return ResponseEntity.ok(desmatamentoService.resumo(ano));
    }

    @PostMapping("/importar")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Map<String, Object>> importar() {
        Long jobId = desmatamentoService.iniciarImportacao();
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(Map.of("jobId", jobId, "status", "PROCESSANDO"));
    }

    @GetMapping("/importar/{jobId}/status")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<ImportJobStatusDTO> status(@PathVariable Long jobId) {
        return ResponseEntity.ok(desmatamentoService.statusImportacao(jobId));
    }
}
