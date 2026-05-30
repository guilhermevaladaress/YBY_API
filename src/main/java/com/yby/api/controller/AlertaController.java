package com.yby.api.controller;

import com.yby.api.dto.AlertaDTO;
import com.yby.api.dto.RiscoDTO;
import com.yby.api.service.AlertaService;
import jakarta.validation.Valid;
import java.time.Year;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping("/desperdicio")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<List<AlertaDTO>> desperdicio(
        @RequestParam(required = false) Integer ano,
        @RequestParam(defaultValue = "10") Integer limite
    ) {
        int anoConsulta = ano == null ? Year.now().getValue() : ano;
        return ResponseEntity.ok(alertaService.desperdicio(anoConsulta, limite));
    }

    @GetMapping("/risco/{municipioId}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<RiscoDTO> risco(@PathVariable Long municipioId) {
        return ResponseEntity.ok(alertaService.risco(municipioId));
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<AlertaDTO> upsert(@Valid @RequestBody AlertaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alertaService.upsert(dto));
    }
}
