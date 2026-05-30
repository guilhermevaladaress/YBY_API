package com.yby.api.controller;

import com.yby.api.dto.MunicipioRankingDTO;
import com.yby.api.dto.PageResponseDTO;
import com.yby.api.dto.TransparenciaMetadadosDTO;
import com.yby.api.repository.MunicipioRepository;
import com.yby.api.service.MunicipioService;
import com.yby.api.service.inteligencia.AlgoritmoMetadata;
import java.util.LinkedHashMap;
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
 * <p>Endpoints publicos somente-leitura (ranking, geojson e metadados) para auditoria
 * cidada dos dados e calculos, sem autenticacao. Registra a versao do algoritmo e os
 * metadados de atualizacao (Lei 12.527/2011 - LAI; LC 131/2009).</p>
 */
@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final MunicipioService municipioService;
    private final MunicipioRepository municipioRepository;

    public PublicController(MunicipioService municipioService, MunicipioRepository municipioRepository) {
        this.municipioService = municipioService;
        this.municipioRepository = municipioRepository;
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

    @GetMapping("/metadados")
    public ResponseEntity<TransparenciaMetadadosDTO> metadados() {
        Map<String, String> pesos = new LinkedHashMap<>();
        pesos.put("desmatamentoRecente", "40%");
        pesos.put("eficienciaGasto", "30%");
        pesos.put("irregularidadesCar", "20%");
        pesos.put("areaElegivel", "10%");

        return ResponseEntity.ok(new TransparenciaMetadadosDTO(
            AlgoritmoMetadata.VERSAO,
            AlgoritmoMetadata.BASE_LEGAL,
            municipioRepository.count(),
            municipioRepository.maxUltimaAtualizacao(),
            pesos));
    }
}
