package com.yby.api.controller;

import com.yby.api.dto.MarcoProjetoDTO;
import com.yby.api.dto.PageResponseDTO;
import com.yby.api.dto.ProjetoJreddDTO;
import com.yby.api.service.ProjetoJreddService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestao de projetos JREDD+ (projeto + metas + marcos).
 *
 * <p>Leitura liberada a GESTOR e SERVIDOR; escrita restrita a GESTOR (auditada).</p>
 */
@RestController
@RequestMapping("/api/v1/projetos")
public class ProjetoJreddController {

    private final ProjetoJreddService projetoJreddService;

    public ProjetoJreddController(ProjetoJreddService projetoJreddService) {
        this.projetoJreddService = projetoJreddService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<PageResponseDTO<ProjetoJreddDTO>> listar(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponseDTO.from(projetoJreddService.listar(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<ProjetoJreddDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(projetoJreddService.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<ProjetoJreddDTO> criar(@Valid @RequestBody ProjetoJreddDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoJreddService.criar(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<ProjetoJreddDTO> atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody ProjetoJreddDTO dto) {
        return ResponseEntity.ok(projetoJreddService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        projetoJreddService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/marcos")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<MarcoProjetoDTO> adicionarMarco(@PathVariable Long id,
                                                          @Valid @RequestBody MarcoProjetoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoJreddService.adicionarMarco(id, dto));
    }

    @PutMapping("/{id}/marcos/{marcoId}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<MarcoProjetoDTO> atualizarMarco(@PathVariable Long id,
                                                          @PathVariable Long marcoId,
                                                          @Valid @RequestBody MarcoProjetoDTO dto) {
        return ResponseEntity.ok(projetoJreddService.atualizarMarco(id, marcoId, dto));
    }

    @DeleteMapping("/{id}/marcos/{marcoId}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> removerMarco(@PathVariable Long id, @PathVariable Long marcoId) {
        projetoJreddService.removerMarco(id, marcoId);
        return ResponseEntity.noContent().build();
    }
}
