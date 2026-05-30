package com.yby.api.controller;

import com.yby.api.dto.InstituicaoCarbonoDTO;
import com.yby.api.dto.MatchCompradorDTO;
import com.yby.api.entity.enums.InstituicaoTipo;
import com.yby.api.service.InstituicaoCarbonoService;
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
 * Cadastro e integracao de instituicoes do mercado de credito de carbono
 * (certificadoras, compradoras, marketplaces) e matching com os creditos do estado.
 *
 * <p>Leitura/matching liberados a GESTOR e SERVIDOR; cadastro/edicao restritos a GESTOR (auditado).</p>
 */
@RestController
@RequestMapping("/api/v1/instituicoes-carbono")
public class InstituicaoCarbonoController {

    private final InstituicaoCarbonoService service;

    public InstituicaoCarbonoController(InstituicaoCarbonoService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<List<InstituicaoCarbonoDTO>> listar(
        @RequestParam(required = false) InstituicaoTipo tipo,
        @RequestParam(defaultValue = "true") boolean somenteAtivas) {
        return ResponseEntity.ok(service.listar(tipo, somenteAtivas));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<InstituicaoCarbonoDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscar(id));
    }

    @GetMapping("/match/credito/{creditoId}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<MatchCompradorDTO> match(@PathVariable Long creditoId,
                                                   @RequestParam(defaultValue = "false") boolean somenteJredd) {
        return ResponseEntity.ok(service.matchCompradores(creditoId, somenteJredd));
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<InstituicaoCarbonoDTO> criar(@Valid @RequestBody InstituicaoCarbonoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<InstituicaoCarbonoDTO> atualizar(@PathVariable Long id,
                                                           @Valid @RequestBody InstituicaoCarbonoDTO dto) {
        return ResponseEntity.ok(service.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
