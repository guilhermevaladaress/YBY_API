package com.yby.api.controller;

import com.yby.api.dto.MunicipioDTO;
import com.yby.api.dto.MunicipioDetalheDTO;
import com.yby.api.dto.MunicipioRankingDTO;
import com.yby.api.dto.MunicipioUpsertDTO;
import com.yby.api.service.MunicipioService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/municipios", "/api/municipios"})
public class MunicipioController {

    private final MunicipioService municipioService;

    public MunicipioController(MunicipioService municipioService) {
        this.municipioService = municipioService;
    }

    @GetMapping
    public ResponseEntity<Page<MunicipioDTO>> listar(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(municipioService.listar(pageable));
    }

    @GetMapping("/ranking")
    public ResponseEntity<Page<MunicipioRankingDTO>> ranking(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        @RequestParam(defaultValue = "score") String ordenar,
        @RequestParam(defaultValue = "desc") String ordem
    ) {
        return ResponseEntity.ok(municipioService.ranking(page, size, ordenar, ordem));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MunicipioDetalheDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(municipioService.buscarDetalhe(id));
    }

    @PostMapping
    public ResponseEntity<MunicipioDTO> criar(@Valid @RequestBody MunicipioUpsertDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(municipioService.criar(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MunicipioDTO> atualizar(@PathVariable Long id, @Valid @RequestBody MunicipioUpsertDTO dto) {
        return ResponseEntity.ok(municipioService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        municipioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
