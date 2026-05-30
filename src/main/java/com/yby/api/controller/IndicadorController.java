package com.yby.api.controller;

import com.yby.api.dto.IndicadorAnualDTO;
import com.yby.api.dto.KpiDTO;
import com.yby.api.service.IndicadorService;
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
@RequestMapping("/api/v1/indicadores")
public class IndicadorController {

    private final IndicadorService indicadorService;

    public IndicadorController(IndicadorService indicadorService) {
        this.indicadorService = indicadorService;
    }

    @GetMapping("/{municipioId}/kpi")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<KpiDTO> kpi(@PathVariable Long municipioId,
                                      @RequestParam Integer anoInicio,
                                      @RequestParam Integer anoFim) {
        return ResponseEntity.ok(indicadorService.kpi(municipioId, anoInicio, anoFim));
    }

    @GetMapping("/{municipioId}/historico")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<List<IndicadorAnualDTO>> historico(
        @PathVariable Long municipioId,
        @RequestParam(required = false) Integer anoInicio,
        @RequestParam(required = false) Integer anoFim
    ) {
        int fim = anoFim == null ? Year.now().getValue() : anoFim;
        int inicio = anoInicio == null ? fim - 5 : anoInicio;
        return ResponseEntity.ok(indicadorService.historico(municipioId, inicio, fim));
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<IndicadorAnualDTO> upsert(@Valid @RequestBody IndicadorAnualDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(indicadorService.upsert(dto));
    }
}
