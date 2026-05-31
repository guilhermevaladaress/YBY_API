package com.yby.api.controller;

import com.yby.api.dto.DashboardDTO;
import com.yby.api.dto.ProjecaoCarbonoDTO;
import com.yby.api.service.CreditoCarbonoService;
import com.yby.api.service.DashboardService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard consolidado do JREDD+ Intelligence para o front-end.
 *
 * <p>Expoe o resumo agregado (KPIs, semaforos, rankings) e a projecao financeira de recebimento
 * de credito rural de carbono nos proximos anos para alimentar os graficos do dashboard.</p>
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CreditoCarbonoService creditoCarbonoService;

    public DashboardController(DashboardService dashboardService,
                              CreditoCarbonoService creditoCarbonoService) {
        this.dashboardService = dashboardService;
        this.creditoCarbonoService = creditoCarbonoService;
    }

    @GetMapping("/resumo")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<DashboardDTO> resumo() {
        return ResponseEntity.ok(dashboardService.resumo());
    }

    @GetMapping("/projecao-carbono")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<ProjecaoCarbonoDTO> projecaoCarbono(
        @RequestParam(required = false) Integer ano,
        @RequestParam(required = false) Long municipioId,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferenciaCotacao
    ) {
        return ResponseEntity.ok(
            creditoCarbonoService.projetarConsolidado(ano, municipioId, dataReferenciaCotacao));
    }
}
