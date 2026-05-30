package com.yby.api.controller;

import com.yby.api.dto.CarbonoDashboardDTO;
import com.yby.api.service.CarbonoDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Painel executivo consolidado de carbono (dashboard profissional).
 *
 * <p>Reune projecao financeira, projetos JREDD+, potencial do Plano Safra e mercado comprador.
 * Leitura liberada a GESTOR e SERVIDOR.</p>
 */
@RestController
@RequestMapping("/api/v1/carbono/dashboard")
public class CarbonoDashboardController {

    private final CarbonoDashboardService carbonoDashboardService;

    public CarbonoDashboardController(CarbonoDashboardService carbonoDashboardService) {
        this.carbonoDashboardService = carbonoDashboardService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<CarbonoDashboardDTO> consolidado(
        @RequestParam(required = false) Integer ano) {
        return ResponseEntity.ok(carbonoDashboardService.consolidar(ano));
    }
}
