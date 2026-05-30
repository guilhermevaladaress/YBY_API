package com.yby.api.controller;

import com.yby.api.dto.RelatorioDTO;
import com.yby.api.dto.RelatorioUpsertDTO;
import com.yby.api.service.RelatorioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/relatorios", "/api/relatorios"})
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping
    public ResponseEntity<Page<RelatorioDTO>> listar(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(relatorioService.listar(pageable));
    }

    @GetMapping("/{municipioId}")
    public ResponseEntity<RelatorioDTO> porMunicipio(@PathVariable Long municipioId) {
        return ResponseEntity.ok(relatorioService.buscarPorMunicipio(municipioId));
    }

    @GetMapping("/{municipioId}/historico")
    public ResponseEntity<List<RelatorioDTO>> historicoPorMunicipio(@PathVariable Long municipioId) {
        return ResponseEntity.ok(relatorioService.historicoPorMunicipio(municipioId));
    }

    @PostMapping
    public ResponseEntity<RelatorioDTO> criar(@Valid @RequestBody RelatorioUpsertDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(relatorioService.criar(dto));
    }

    @PutMapping("/item/{id}")
    public ResponseEntity<RelatorioDTO> atualizar(@PathVariable Long id, @Valid @RequestBody RelatorioUpsertDTO dto) {
        return ResponseEntity.ok(relatorioService.atualizar(id, dto));
    }

    @DeleteMapping("/item/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        relatorioService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/municipio/{municipioId}")
    public ResponseEntity<Void> deletarPorMunicipio(@PathVariable Long municipioId) {
        relatorioService.deletarPorMunicipio(municipioId);
        return ResponseEntity.noContent().build();
    }
}
