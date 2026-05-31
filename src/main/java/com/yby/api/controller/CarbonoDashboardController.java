package com.yby.api.controller;

import com.yby.api.dto.CarbonoDashboardDTO;
import com.yby.api.service.CarbonoDashboardService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Painel executivo consolidado de carbono (dashboard profissional).
 *
 * <p>Reúne projeção financeira (USD e R$), projetos JREDD+, potencial do Plano Safra,
 * mercado comprador, gráficos de projeção e caixa informativa sobre crédito de carbono.
 * Leitura liberada a GESTOR e SERVIDOR.</p>
 */
@RestController
@RequestMapping("/api/v1/carbono/dashboard")
public class CarbonoDashboardController {

    private final CarbonoDashboardService carbonoDashboardService;

    public CarbonoDashboardController(CarbonoDashboardService carbonoDashboardService) {
        this.carbonoDashboardService = carbonoDashboardService;
    }

    /**
     * Retorna o painel executivo consolidado de carbono.
     *
     * @param ano                   filtra a série anual para anos &gt;= ano; opcional
     * @param dataReferenciaCotacao data da cotação USD/BRL para conversão (ISO-8601, ex: 2025-06-01); padrão = hoje
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<CarbonoDashboardDTO> consolidado(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferenciaCotacao) {
        return ResponseEntity.ok(carbonoDashboardService.consolidar(ano, dataReferenciaCotacao));
    }
}
