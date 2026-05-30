package com.yby.api.controller;

import com.yby.api.dto.EstatisticaDashboardDTO;
import com.yby.api.dto.EstatisticaGeralDTO;
import com.yby.api.service.EstatisticaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/estatisticas", "/api/estatisticas"})
public class EstatisticaController {

    private final EstatisticaService estatisticaService;

    public EstatisticaController(EstatisticaService estatisticaService) {
        this.estatisticaService = estatisticaService;
    }

    @GetMapping("/geral")
    public ResponseEntity<EstatisticaGeralDTO> geral() {
        return ResponseEntity.ok(estatisticaService.geral());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<EstatisticaDashboardDTO> dashboard() {
        return ResponseEntity.ok(estatisticaService.dashboard());
    }
}
