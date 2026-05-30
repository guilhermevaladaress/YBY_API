package com.yby.api.controller;

import com.yby.api.dto.AlocacaoRequestDTO;
import com.yby.api.dto.AlocacaoResponseDTO;
import com.yby.api.dto.CarbonoEvitadoDTO;
import com.yby.api.dto.DesperdicioInteligenteDTO;
import com.yby.api.dto.EquidadeTerritorialDTO;
import com.yby.api.dto.KpiMultidimensionalDTO;
import com.yby.api.dto.ProjecaoDesmatamentoDTO;
import com.yby.api.dto.RiscoPreditivoDTO;
import com.yby.api.dto.RoiDesmatamentoEvitadoDTO;
import com.yby.api.dto.SemaforoBiomaDTO;
import com.yby.api.dto.TendenciaDesmatamentoDTO;
import com.yby.api.service.inteligencia.AlocacaoService;
import com.yby.api.service.inteligencia.CarbonoEvitadoService;
import com.yby.api.service.inteligencia.DesperdicioInteligenteService;
import com.yby.api.service.inteligencia.EquidadeService;
import com.yby.api.service.inteligencia.KpiMultidimensionalService;
import com.yby.api.service.inteligencia.ProjecaoDesmatamentoService;
import com.yby.api.service.inteligencia.RiscoPreditivoService;
import com.yby.api.service.inteligencia.RoiDesmatamentoEvitadoService;
import com.yby.api.service.inteligencia.SemaforoBiomaService;
import com.yby.api.service.inteligencia.TendenciaService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Regras de Negocio Inovadoras (docs/REGRAS_NEGOCIO_INOVACAO.md).
 *
 * <p>Expoe as extensoes de inteligencia para a gestao publica ambiental. Leitura liberada a
 * GESTOR e SERVIDOR; a simulacao de alocacao (decisao orcamentaria) e restrita a GESTOR.</p>
 */
@RestController
@RequestMapping("/api/v1/inteligencia")
public class InteligenciaController {

    private final TendenciaService tendenciaService;
    private final KpiMultidimensionalService kpiMultidimensionalService;
    private final SemaforoBiomaService semaforoBiomaService;
    private final RiscoPreditivoService riscoPreditivoService;
    private final DesperdicioInteligenteService desperdicioInteligenteService;
    private final EquidadeService equidadeService;
    private final AlocacaoService alocacaoService;
    private final CarbonoEvitadoService carbonoEvitadoService;
    private final ProjecaoDesmatamentoService projecaoDesmatamentoService;
    private final RoiDesmatamentoEvitadoService roiDesmatamentoEvitadoService;

    public InteligenciaController(TendenciaService tendenciaService,
                                  KpiMultidimensionalService kpiMultidimensionalService,
                                  SemaforoBiomaService semaforoBiomaService,
                                  RiscoPreditivoService riscoPreditivoService,
                                  DesperdicioInteligenteService desperdicioInteligenteService,
                                  EquidadeService equidadeService,
                                  AlocacaoService alocacaoService,
                                  CarbonoEvitadoService carbonoEvitadoService,
                                  ProjecaoDesmatamentoService projecaoDesmatamentoService,
                                  RoiDesmatamentoEvitadoService roiDesmatamentoEvitadoService) {
        this.tendenciaService = tendenciaService;
        this.kpiMultidimensionalService = kpiMultidimensionalService;
        this.semaforoBiomaService = semaforoBiomaService;
        this.riscoPreditivoService = riscoPreditivoService;
        this.desperdicioInteligenteService = desperdicioInteligenteService;
        this.equidadeService = equidadeService;
        this.alocacaoService = alocacaoService;
        this.carbonoEvitadoService = carbonoEvitadoService;
        this.projecaoDesmatamentoService = projecaoDesmatamentoService;
        this.roiDesmatamentoEvitadoService = roiDesmatamentoEvitadoService;
    }

    /** RN-101-A - Score de Prioridade com Tendencia. */
    @GetMapping("/tendencia/{municipioId}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public TendenciaDesmatamentoDTO tendencia(@PathVariable Long municipioId,
                                              @RequestParam(required = false) Integer ano) {
        return tendenciaService.calcular(municipioId, ano);
    }

    /** RN-103-B - KPI Multidimensional. */
    @GetMapping("/kpi-multidimensional/{municipioId}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public KpiMultidimensionalDTO kpiMultidimensional(@PathVariable Long municipioId,
                                                      @RequestParam Integer ano,
                                                      @RequestParam(required = false) BigDecimal pesoAmbiental,
                                                      @RequestParam(required = false) BigDecimal pesoSocial,
                                                      @RequestParam(required = false) BigDecimal pesoFiscal) {
        return kpiMultidimensionalService.calcular(municipioId, ano, pesoAmbiental, pesoSocial, pesoFiscal);
    }

    /** RN-106-A - Semaforo Bioma-Sensivel + RN-600 Freshness. */
    @GetMapping("/semaforo-bioma/{municipioId}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public SemaforoBiomaDTO semaforoBioma(@PathVariable Long municipioId) {
        return semaforoBiomaService.avaliar(municipioId);
    }

    /** RN-108-A - Risco Preditivo com Historico. */
    @GetMapping("/risco-preditivo/{municipioId}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public RiscoPreditivoDTO riscoPreditivo(@PathVariable Long municipioId) {
        return riscoPreditivoService.avaliar(municipioId);
    }

    /** RN-107-A - Alerta de Desperdicio Inteligente (benchmarking). */
    @GetMapping("/desperdicio")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public List<DesperdicioInteligenteDTO> desperdicio(@RequestParam(required = false) Integer ano) {
        return desperdicioInteligenteService.avaliar(ano);
    }

    /** RN-500 - Equidade Territorial. */
    @GetMapping("/equidade")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public EquidadeTerritorialDTO equidade(@RequestParam(required = false) Integer ano) {
        return equidadeService.avaliar(ano);
    }

    /** RN-200 - Otimizacao de Alocacao (decisao orcamentaria, restrita a GESTOR). */
    @PostMapping("/alocacao")
    @PreAuthorize("hasRole('GESTOR')")
    public AlocacaoResponseDTO alocacao(@Valid @RequestBody AlocacaoRequestDTO request) {
        return alocacaoService.otimizar(request);
    }

    /** Carbono Evitado (avoided emissions) - nucleo do JREDD+. */
    @GetMapping("/carbono-evitado/{municipioId}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public CarbonoEvitadoDTO carbonoEvitado(@PathVariable Long municipioId,
                                            @RequestParam(required = false) Integer ano,
                                            @RequestParam(required = false) BigDecimal preco) {
        return carbonoEvitadoService.avaliar(municipioId, ano, preco);
    }

    /** Projecao de Desmatamento (tendencia historica -> proximos anos). */
    @GetMapping("/projecao-desmatamento/{municipioId}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public ProjecaoDesmatamentoDTO projecaoDesmatamento(@PathVariable Long municipioId,
                                                        @RequestParam(required = false) Integer horizonteAnos) {
        return projecaoDesmatamentoService.avaliar(municipioId, horizonteAnos);
    }

    /** ROI de Desmatamento Evitado (financeiro: JREDD+ vs. conversao da area). */
    @GetMapping("/roi-desmatamento-evitado/{municipioId}")
    @PreAuthorize("hasAnyRole('GESTOR','SERVIDOR')")
    public RoiDesmatamentoEvitadoDTO roiDesmatamentoEvitado(
        @PathVariable Long municipioId,
        @RequestParam(required = false) Integer ano,
        @RequestParam(required = false) BigDecimal preco,
        @RequestParam(required = false) BigDecimal valorAgropecuariaHaAno) {
        return roiDesmatamentoEvitadoService.avaliar(municipioId, ano, preco, valorAgropecuariaHaAno);
    }
}
