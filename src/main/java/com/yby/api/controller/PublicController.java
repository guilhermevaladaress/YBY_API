package com.yby.api.controller;

import com.yby.api.dto.MunicipioRankingDTO;
import com.yby.api.dto.PageResponseDTO;
import com.yby.api.service.MunicipioService;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RN-300 - Transparencia e Open Data.
 *
 * <p>Endpoints publicos somente-leitura (ranking e geojson) para auditoria
 * cidada dos dados e calculos, sem autenticacao (Lei 12.527/2011 - LAI;
 * LC 131/2009).</p>
 */
@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final MunicipioService municipioService;

    public PublicController(MunicipioService municipioService) {
        this.municipioService = municipioService;
    }

    @GetMapping("/ranking")
    public ResponseEntity<PageResponseDTO<MunicipioRankingDTO>> ranking(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size,
        @RequestParam(defaultValue = "score") String ordenar,
        @RequestParam(defaultValue = "desc") String ordem
    ) {
        return ResponseEntity.ok(PageResponseDTO.from(municipioService.ranking(page, size, ordenar, ordem)));
    }

    @GetMapping(value = "/geojson", produces = "application/geo+json")
    public ResponseEntity<Map<String, Object>> geoJson(@RequestParam(required = false) String semaforo) {
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/geo+json"))
            .body(municipioService.geoJson(semaforo));
    }
}
