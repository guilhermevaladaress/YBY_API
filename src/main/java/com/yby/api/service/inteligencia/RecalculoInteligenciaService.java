package com.yby.api.service.inteligencia;

import com.yby.api.dto.CarbonoEvitadoDTO;
import com.yby.api.dto.ProjecaoArmazenadaDTO;
import com.yby.api.dto.ProjecaoDesmatamentoDTO;
import com.yby.api.dto.RecalculoInteligenciaDTO;
import com.yby.api.dto.RiscoPreditivoDTO;
import com.yby.api.dto.TendenciaDesmatamentoDTO;
import com.yby.api.dto.ValorAnualDTO;
import com.yby.api.entity.Municipio;
import com.yby.api.entity.ProjecaoInteligencia;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.repository.DesmatamentoRepository;
import com.yby.api.repository.MunicipioRepository;
import com.yby.api.repository.ProjecaoInteligenciaRepository;
import com.yby.api.service.AuditService;
import com.yby.api.service.MunicipioService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Recalcula e ARMAZENA os resultados preditivos de inteligencia por municipio.
 *
 * <p>Para cada municipio combina a serie historica de desmatamento (PRODES) com os focos de calor
 * recentes do INPE e roda os algoritmos ja existentes (tendencia RN-101-A, projecao, risco RN-108-A,
 * carbono evitado JREDD+). Os calculos sao ancorados no ultimo ano COM dado de desmatamento (a serie
 * e anual e defasada), evitando intervalos vazios no futuro. O resultado e persistido em
 * {@code projecoes_inteligencia} (snapshot por municipio) e os campos derivados de risco/pendencias
 * sao gravados no proprio municipio. Idempotente: nao acumula ajustes entre execucoes.</p>
 */
@Service
public class RecalculoInteligenciaService {

    private static final Logger log = LoggerFactory.getLogger(RecalculoInteligenciaService.class);
    private static final BigDecimal CEM = new BigDecimal("100");

    private final MunicipioRepository municipioRepository;
    private final DesmatamentoRepository desmatamentoRepository;
    private final ProjecaoInteligenciaRepository projecaoRepository;
    private final TendenciaService tendenciaService;
    private final ProjecaoDesmatamentoService projecaoDesmatamentoService;
    private final RiscoPreditivoService riscoPreditivoService;
    private final CarbonoEvitadoService carbonoEvitadoService;
    private final FocoCalorService focoCalorService;
    private final MunicipioService municipioService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public RecalculoInteligenciaService(MunicipioRepository municipioRepository,
                                        DesmatamentoRepository desmatamentoRepository,
                                        ProjecaoInteligenciaRepository projecaoRepository,
                                        TendenciaService tendenciaService,
                                        ProjecaoDesmatamentoService projecaoDesmatamentoService,
                                        RiscoPreditivoService riscoPreditivoService,
                                        CarbonoEvitadoService carbonoEvitadoService,
                                        FocoCalorService focoCalorService,
                                        MunicipioService municipioService,
                                        AuditService auditService,
                                        ObjectMapper objectMapper) {
        this.municipioRepository = municipioRepository;
        this.desmatamentoRepository = desmatamentoRepository;
        this.projecaoRepository = projecaoRepository;
        this.tendenciaService = tendenciaService;
        this.projecaoDesmatamentoService = projecaoDesmatamentoService;
        this.riscoPreditivoService = riscoPreditivoService;
        this.carbonoEvitadoService = carbonoEvitadoService;
        this.focoCalorService = focoCalorService;
        this.municipioService = municipioService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    /** Recalcula e persiste o snapshot preditivo de todos os municipios. */
    @Transactional
    public RecalculoInteligenciaDTO recalcularTodos() {
        Integer anoMax = desmatamentoRepository.maxAno();
        int anoBase = (anoMax == null || anoMax == 0) ? Year.now().getValue() - 1 : anoMax;

        long focosEstado = focoCalorService.focosEstadoAnoCorrente();
        BigDecimal totalDesmatamentoEstado = desmatamentoRepository.findTotalByAno(anoBase);

        List<Municipio> municipios = municipioRepository.findAll();
        int processados = 0;
        for (Municipio municipio : municipios) {
            recalcularMunicipio(municipio, anoBase, focosEstado, totalDesmatamentoEstado);
            processados++;
        }

        RecalculoInteligenciaDTO resultado = new RecalculoInteligenciaDTO(
            processados, anoBase, focosEstado, AlgoritmoMetadata.VERSAO, OffsetDateTime.now());
        log.info("Recalculo de inteligencia concluido: {} municipios, anoBase={}, focosTO={}.",
            processados, anoBase, focosEstado);
        auditService.registrarEscritaGestor("RECALCULO", "projecoes_inteligencia", "TODOS", resultado);
        return resultado;
    }

    private void recalcularMunicipio(Municipio municipio, int anoBase, long focosEstado,
                                     BigDecimal totalDesmatamentoEstado) {
        Long id = municipio.getId();

        // Serie historica de desmatamento ancorada no ultimo ano com dado.
        List<ValorAnualDTO> historico = new ArrayList<>();
        for (int a = anoBase - (ProjecaoDesmatamentoService.ANOS_HISTORICO - 1); a <= anoBase; a++) {
            historico.add(new ValorAnualDTO(a, areaNoAno(id, a)));
        }
        BigDecimal areaAnoBase = historico.getLast().valor();
        BigDecimal areaAnterior = areaNoAno(id, anoBase - 1);

        TendenciaDesmatamentoDTO tendencia =
            tendenciaService.avaliar(id, anoBase, areaAnoBase, areaAnterior);
        ProjecaoDesmatamentoDTO projecao =
            projecaoDesmatamentoService.calcular(id, historico, ProjecaoDesmatamentoService.HORIZONTE_PADRAO);
        RiscoPreditivoDTO risco = riscoPreditivoService.avaliar(id);
        CarbonoEvitadoDTO carbono = carbonoEvitadoService.avaliar(id, anoBase, null);

        // Focos do INPE (estado) distribuidos proporcionalmente ao desmatamento recente do municipio.
        int focosMunicipio = distribuirFocos(focosEstado, areaAnoBase, totalDesmatamentoEstado);

        BigDecimal scoreProjetado = scoreProjetado(municipio.getScorePrioridade(), tendencia.ajusteScore());

        // Persistencia no municipio: risco, pendencias e KPI (sem alterar score/semaforo de ranking).
        municipio.setNotaRisco(risco.notaRiscoFinal());
        municipio.setKpiRetorno(municipioService.calcularKpiRetorno(id));
        municipio.setPendenciasResumo(resumoPendencias(risco.pendencias()));
        municipioRepository.save(municipio);

        // Snapshot preditivo (upsert por municipio).
        ProjecaoInteligencia snapshot = projecaoRepository.findByMunicipioId(id)
            .orElseGet(ProjecaoInteligencia::new);
        snapshot.setMunicipio(municipio);
        snapshot.setAnoBase(anoBase);
        snapshot.setTendenciaPercent(tendencia.tendenciaDesmatamento());
        snapshot.setStatusTendencia(tendencia.statusTendencia().name());
        snapshot.setAjusteScore(tendencia.ajusteScore());
        snapshot.setTaxaMediaAnual(projecao.taxaMediaAnual());
        snapshot.setMediaHistoricaHa(projecao.mediaHistoricaHa());
        snapshot.setHorizonteAnos(projecao.horizonteAnos());
        snapshot.setProjecaoJson(serializarSerie(projecao));
        snapshot.setNotaRisco(risco.notaRiscoFinal());
        snapshot.setSemaforoRisco(risco.semaforoRisco());
        snapshot.setScoreProjetado(scoreProjetado);
        snapshot.setTco2eEvitado(carbono.tco2eEvitado());
        snapshot.setValorPotencialReais(carbono.valorPotencialReais());
        snapshot.setFocosCalorAno(focosMunicipio);
        snapshot.setVersaoAlgoritmo(AlgoritmoMetadata.VERSAO);
        snapshot.setCalculadoEm(OffsetDateTime.now());
        projecaoRepository.save(snapshot);
    }

    @Transactional(readOnly = true)
    public ProjecaoArmazenadaDTO buscarProjecao(Long municipioId) {
        ProjecaoInteligencia p = projecaoRepository.findByMunicipioId(municipioId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Projecao nao calculada para o municipio; rode /api/v1/inteligencia/recalcular."));
        Municipio m = p.getMunicipio();
        return new ProjecaoArmazenadaDTO(
            m.getId(), m.getNome(), m.getCodigoIbge(), p.getAnoBase(), p.getTendenciaPercent(),
            p.getStatusTendencia(), p.getAjusteScore(), p.getTaxaMediaAnual(), p.getMediaHistoricaHa(),
            p.getHorizonteAnos(), p.getProjecaoJson(), p.getNotaRisco(), p.getSemaforoRisco(),
            p.getScoreProjetado(), p.getTco2eEvitado(), p.getValorPotencialReais(), p.getFocosCalorAno(),
            p.getVersaoAlgoritmo(), p.getCalculadoEm());
    }

    private int distribuirFocos(long focosEstado, BigDecimal areaMunicipio, BigDecimal totalEstado) {
        if (focosEstado <= 0 || totalEstado == null || totalEstado.compareTo(BigDecimal.ZERO) <= 0
            || areaMunicipio == null) {
            return 0;
        }
        return areaMunicipio.multiply(BigDecimal.valueOf(focosEstado))
            .divide(totalEstado, 0, RoundingMode.HALF_UP)
            .intValue();
    }

    private BigDecimal scoreProjetado(BigDecimal scoreBase, int ajuste) {
        BigDecimal base = scoreBase == null ? new BigDecimal("50") : scoreBase;
        return base.add(BigDecimal.valueOf(ajuste))
            .max(BigDecimal.ZERO).min(CEM).setScale(2, RoundingMode.HALF_UP);
    }

    private String resumoPendencias(List<String> pendencias) {
        if (pendencias == null || pendencias.isEmpty()) {
            return null;
        }
        String resumo = String.join(" | ", pendencias);
        return resumo.length() > 2000 ? resumo.substring(0, 2000) : resumo;
    }

    private String serializarSerie(ProjecaoDesmatamentoDTO projecao) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("serieHistorica", projecao.serieHistorica());
        payload.put("projecao", projecao.projecao());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.warn("Falha ao serializar projecao do municipio {}: {}",
                projecao.municipioId(), e.getMessage());
            return null;
        }
    }

    private BigDecimal areaNoAno(Long municipioId, int ano) {
        BigDecimal area = desmatamentoRepository.sumAreaByMunicipioAndPeriodo(
            municipioId, LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31));
        return area == null ? BigDecimal.ZERO : area;
    }
}
