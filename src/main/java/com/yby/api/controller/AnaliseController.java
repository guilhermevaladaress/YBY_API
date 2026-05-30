package com.yby.api.controller;

import com.yby.api.dto.ProntidaoDTO;
import com.yby.api.dto.RetornoDTO;
import com.yby.api.dto.RiscoDTO;
import com.yby.api.service.AnaliseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/analise", "/api/analise"})
public class AnaliseController {

    private final AnaliseService analiseService;

    public AnaliseController(AnaliseService analiseService) {
        this.analiseService = analiseService;
    }

    @GetMapping("/risco/{municipioId}")
    public ResponseEntity<RiscoDTO> risco(@PathVariable Long municipioId) {
        return ResponseEntity.ok(analiseService.risco(municipioId));
    }

    @GetMapping("/prontidao/{municipioId}")
    public ResponseEntity<ProntidaoDTO> prontidao(@PathVariable Long municipioId) {
        return ResponseEntity.ok(analiseService.prontidao(municipioId));
    }

    @GetMapping("/retorno/{municipioId}")
    public ResponseEntity<RetornoDTO> retorno(@PathVariable Long municipioId) {
        return ResponseEntity.ok(analiseService.retorno(municipioId));
    }
}
