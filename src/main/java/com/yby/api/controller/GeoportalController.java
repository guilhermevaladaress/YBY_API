package com.yby.api.controller;

import com.yby.api.dto.AreaCarbonoEstudoDTO;
import com.yby.api.dto.CamadaGeoportalDTO;
import com.yby.api.integration.seplan.SeplanFeatureCollectionDTO;
import com.yby.api.service.GeoportalService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Integracao com o Geoportal da SEPLAN-TO para o estudo de areas de carbono.
 *
 * <p>Expoe o catalogo de camadas relevantes, a consulta direta ao GeoServer (WFS) e a sugestao
 * inteligente de areas prioritarias. Leitura liberada a GESTOR e SERVIDOR.</p>
 */
@RestController
@RequestMapping("/api/v1/geoportal")
public class GeoportalController {

    private final GeoportalService geoportalService;

    public GeoportalController(GeoportalService geoportalService) {
        this.geoportalService = geoportalService;
    }

    /** Catalogo curado das camadas do Geoportal SEPLAN uteis para estudo de carbono. */
    @GetMapping("/camadas")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<List<CamadaGeoportalDTO>> camadas() {
        return ResponseEntity.ok(geoportalService.catalogoCamadas());
    }

    /** Consulta direta a uma camada do GeoServer da SEPLAN (WFS GetFeature). */
    @GetMapping("/camadas/dados")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<SeplanFeatureCollectionDTO> dadosCamada(
        @RequestParam String typeName,
        @RequestParam(required = false) String cqlFilter,
        @RequestParam(defaultValue = "100") int maxFeatures) {
        return ResponseEntity.ok(geoportalService.consultarCamada(typeName, cqlFilter, maxFeatures));
    }

    /** Sugere areas prioritarias para estudo de carbono cruzando dados locais e o geoportal. */
    @GetMapping("/areas-carbono/sugestoes")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ResponseEntity<List<AreaCarbonoEstudoDTO>> sugestoes(
        @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(geoportalService.sugerirAreas(limite));
    }
}
