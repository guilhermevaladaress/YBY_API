package com.yby.api.controller;

import com.yby.api.dto.LinhaCreditoSafraDTO;
import com.yby.api.dto.SimulacaoSafraDTO;
import com.yby.api.dto.SimulacaoSafraRequestDTO;
import com.yby.api.service.PlanoSafraService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Plano Safra 2026: linhas de credito rural (RenovAgro/ABC+, Pronaf) e simulacao
 * de financiamento integrada ao potencial de geracao de credito de carbono.
 *
 * <p>Leitura/simulacao liberadas a GESTOR e SERVIDOR; cadastro/edicao restritos a GESTOR (auditado).</p>
 */
@RestController
@RequestMapping("/api/v1/plano-safra")
public class PlanoSafraController {

    private final PlanoSafraService planoSafraService;

    public PlanoSafraController(PlanoSafraService planoSafraService) {
        this.planoSafraService = planoSafraService;
    }

    @GetMapping("/linhas")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<List<LinhaCreditoSafraDTO>> listar(
        @RequestParam(defaultValue = "true") boolean somenteAtivas) {
        return ResponseEntity.ok(planoSafraService.listar(somenteAtivas));
    }

    @GetMapping("/linhas/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<LinhaCreditoSafraDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(planoSafraService.buscar(id));
    }

    @PostMapping("/simulacao")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<SimulacaoSafraDTO> simular(@Valid @RequestBody SimulacaoSafraRequestDTO req) {
        return ResponseEntity.ok(planoSafraService.simular(req));
    }

    @PostMapping("/linhas")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<LinhaCreditoSafraDTO> criar(@Valid @RequestBody LinhaCreditoSafraDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planoSafraService.criar(dto));
    }

    @PutMapping("/linhas/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<LinhaCreditoSafraDTO> atualizar(@PathVariable Long id,
                                                          @Valid @RequestBody LinhaCreditoSafraDTO dto) {
        return ResponseEntity.ok(planoSafraService.atualizar(id, dto));
    }

    @DeleteMapping("/linhas/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        planoSafraService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
