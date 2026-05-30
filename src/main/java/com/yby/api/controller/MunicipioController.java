package com.yby.api.controller;

import com.yby.api.dto.MunicipioDTO;
import com.yby.api.dto.MunicipioDetalheDTO;
import com.yby.api.dto.MunicipioRankingDTO;
import com.yby.api.dto.MunicipioUpdateDTO;
import com.yby.api.dto.MunicipioUpsertDTO;
import com.yby.api.dto.PageResponseDTO;
import com.yby.api.service.MunicipioService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

@RestController
@RequestMapping("/api/v1/municipios")
public class MunicipioController {

    private final MunicipioService municipioService;

    public MunicipioController(MunicipioService municipioService) {
        this.municipioService = municipioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<PageResponseDTO<MunicipioDTO>> listar(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(PageResponseDTO.from(municipioService.listar(pageable)));
    }

    @GetMapping("/ranking")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<PageResponseDTO<MunicipioRankingDTO>> ranking(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        @RequestParam(defaultValue = "score") String ordenar,
        @RequestParam(defaultValue = "desc") String ordem
    ) {
        return ResponseEntity.ok(PageResponseDTO.from(municipioService.ranking(page, size, ordenar, ordem)));
    }

    @GetMapping(value = "/geojson", produces = "application/geo+json")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<Map<String, Object>> geoJson(@RequestParam(required = false) String semaforo) {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/geo+json"))
            .body(municipioService.geoJson(semaforo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<MunicipioDetalheDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(municipioService.buscarDetalhe(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<MunicipioDTO> criar(@Valid @RequestBody MunicipioUpsertDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(municipioService.criar(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<MunicipioDTO> atualizarParcial(@PathVariable Long id, @Valid @RequestBody MunicipioUpdateDTO dto) {
        return ResponseEntity.ok(municipioService.atualizarParcial(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        municipioService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
