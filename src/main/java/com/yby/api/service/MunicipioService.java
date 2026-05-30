package com.yby.api.service;

import com.yby.api.dto.MunicipioDTO;
import com.yby.api.dto.MunicipioDetalheDTO;
import com.yby.api.dto.MunicipioRankingDTO;
import com.yby.api.dto.MunicipioUpdateDTO;
import com.yby.api.dto.MunicipioUpsertDTO;
import com.yby.api.entity.Municipio;
import com.yby.api.entity.enums.Semaforo;
import com.yby.api.exception.BusinessException;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.mapper.MunicipioMapper;
import com.yby.api.repository.AlertaRepository;
import com.yby.api.repository.DesmatamentoRepository;
import com.yby.api.repository.IndicadorRepository;
import com.yby.api.repository.MunicipioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class MunicipioService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final MunicipioRepository municipioRepository;
    private final IndicadorRepository indicadorRepository;
    private final DesmatamentoRepository desmatamentoRepository;
    private final AlertaRepository alertaRepository;
    private final AuditService auditService;
    private final MunicipioMapper municipioMapper;
    private final ObjectMapper objectMapper;

    public MunicipioService(MunicipioRepository municipioRepository,
                           IndicadorRepository indicadorRepository,
                           DesmatamentoRepository desmatamentoRepository,
                           AlertaRepository alertaRepository,
                           AuditService auditService,
                           MunicipioMapper municipioMapper,
                           ObjectMapper objectMapper) {
        this.municipioRepository = municipioRepository;
        this.indicadorRepository = indicadorRepository;
        this.desmatamentoRepository = desmatamentoRepository;
        this.alertaRepository = alertaRepository;
        this.auditService = auditService;
        this.municipioMapper = municipioMapper;
        this.objectMapper = objectMapper;
    }

    public Page<MunicipioDTO> listar(Pageable pageable) {
        return municipioRepository.findAll(pageable)
            .map(municipio -> municipioMapper.toDTO(municipio, calcularKpiRetorno(municipio.getId())));
    }

    public Page<MunicipioRankingDTO> ranking(int page, int size, String ordenar, String ordem) {
        Sort.Direction direction = "asc".equalsIgnoreCase(ordem) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String campo = switch ((ordenar == null ? "score" : ordenar).toLowerCase()) {
            case "nome" -> "nome";
            case "area" -> "areaHa";
            default -> "scorePrioridade";
        };

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, campo));
        Page<Municipio> municipios = municipioRepository.findAll(pageRequest);
        List<MunicipioRankingDTO> content = municipios.getContent().stream()
            .map(m -> municipioMapper.toRankingDTO(m, calcularKpiRetorno(m.getId())))
            .toList();
        return new PageImpl<>(content, pageRequest, municipios.getTotalElements());
    }

    public MunicipioDetalheDTO buscarDetalhe(Long id) {
        Municipio municipio = buscarMunicipio(id);
        BigDecimal notaRisco = calcularNotaRisco(id);
        BigDecimal kpi = calcularKpiRetorno(id);

        List<String> pendencias = alertaRepository.findByMunicipioIdAndAtivoTrueOrderByCreatedAtDesc(id)
            .stream()
            .map(a -> a.getTipo().name() + ": " + a.getDescricao())
            .toList();

        return municipioMapper.toDetalheDTO(municipio, notaRisco, kpi, pendencias);
    }

    public Map<String, Object> geoJson(String semaforo) {
        List<Municipio> municipios;
        if (semaforo == null || semaforo.isBlank()) {
            municipios = municipioRepository.findAll();
        } else {
            municipios = municipioRepository.findBySemaforo(Semaforo.valueOf(semaforo.toUpperCase()));
        }

        List<Map<String, Object>> features = new ArrayList<>();
        for (Municipio municipio : municipios) {
            Map<String, Object> feature = new HashMap<>();
            feature.put("type", "Feature");

            Map<String, Object> props = new HashMap<>();
            props.put("id", municipio.getId());
            props.put("nome", municipio.getNome());
            props.put("codigoIbge", municipio.getCodigoIbge());
            props.put("scorePrioridade", municipio.getScorePrioridade());
            props.put("semaforo", municipio.getSemaforo().name().toLowerCase());
            props.put("kpiRetorno", calcularKpiRetorno(municipio.getId()));
            feature.put("properties", props);

            feature.put("geometry", parseGeoJsonGeometry(municipio.getGeojsonPolygon()));
            features.add(feature);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("type", "FeatureCollection");
        response.put("features", features);
        return response;
    }

    @Transactional
    public MunicipioDTO criar(MunicipioUpsertDTO dto) {
        if (municipioRepository.existsByCodigoIbge(dto.codigoIbge())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ja existe municipio com este codigo IBGE");
        }
        Municipio municipio = new Municipio();
        municipioMapper.applyUpsert(municipio, dto);
        atualizarMetricasDerivadas(municipio);
        Municipio saved = municipioRepository.save(municipio);
        auditService.registrarEscritaGestor("CREATE", "municipios", String.valueOf(saved.getId()), dto);
        return municipioMapper.toDTO(saved, calcularKpiRetorno(saved.getId()));
    }

    @Transactional
    public MunicipioDTO atualizar(Long id, MunicipioUpsertDTO dto) {
        Municipio municipio = buscarMunicipio(id);
        if (!municipio.getCodigoIbge().equals(dto.codigoIbge()) && municipioRepository.existsByCodigoIbge(dto.codigoIbge())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ja existe municipio com este codigo IBGE");
        }

        municipioMapper.applyUpsert(municipio, dto);
        atualizarMetricasDerivadas(municipio);
        Municipio saved = municipioRepository.save(municipio);
        auditService.registrarEscritaGestor("UPDATE", "municipios", String.valueOf(saved.getId()), dto);
        return municipioMapper.toDTO(saved, calcularKpiRetorno(saved.getId()));
    }

    @Transactional
    public MunicipioDTO atualizarParcial(Long id, MunicipioUpdateDTO dto) {
        Municipio municipio = buscarMunicipio(id);

        if (dto.codigoIbge() != null
            && !dto.codigoIbge().equals(municipio.getCodigoIbge())
            && municipioRepository.existsByCodigoIbge(dto.codigoIbge())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ja existe municipio com este codigo IBGE");
        }

        Map<String, Object> antes = snapshot(municipio);
        municipioMapper.applyUpdate(municipio, dto);
        atualizarMetricasDerivadas(municipio);
        Municipio saved = municipioRepository.save(municipio);
        // RN-007-A: alteracao manual registra antes/depois, fonte e justificativa.
        auditService.registrarAlteracao("UPDATE", "municipios", String.valueOf(saved.getId()),
            antes, snapshot(saved), AuditService.FonteAlteracao.MANUAL,
            "Correcao/atualizacao manual de municipio por GESTOR");
        return municipioMapper.toDTO(saved, calcularKpiRetorno(saved.getId()));
    }

    private Map<String, Object> snapshot(Municipio municipio) {
        Map<String, Object> snap = new HashMap<>();
        snap.put("nome", municipio.getNome());
        snap.put("codigoIbge", municipio.getCodigoIbge());
        snap.put("scorePrioridade", municipio.getScorePrioridade());
        snap.put("semaforo", municipio.getSemaforo());
        snap.put("bioma", municipio.getBioma());
        snap.put("areaHa", municipio.getAreaHa());
        return snap;
    }

    @Transactional
    public void deletar(Long id) {
        Municipio municipio = buscarMunicipio(id);
        municipioRepository.delete(municipio);
        auditService.registrarEscritaGestor("DELETE", "municipios", String.valueOf(id), municipio.getNome());
    }

    public Municipio buscarMunicipio(Long id) {
        return municipioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Municipio nao encontrado"));
    }

    public BigDecimal calcularKpiRetorno(Long municipioId) {
        return indicadorRepository.findTopByMunicipioIdOrderByAnoDesc(municipioId)
            .map(indicador -> safeKpi(indicador.getResultadoAmbiental(), indicador.getGastoPublico()))
            .orElse(null);
    }

    public BigDecimal calcularNotaRisco(Long municipioId) {
        BigDecimal scoreAlertas = BigDecimal.valueOf(
            alertaRepository.findByMunicipioIdAndAtivoTrueOrderByCreatedAtDesc(municipioId)
                .stream()
                .mapToInt(alerta -> switch (alerta.getGravidade()) {
                    case ALTA -> 3;
                    case MEDIA -> 2;
                    case BAIXA -> 1;
                })
                .sum()
        );

        BigDecimal desmatamentoRecente = desmatamentoRepository.sumAreaByMunicipioAndPeriodo(
            municipioId,
            LocalDate.now().minusMonths(12),
            LocalDate.now()
        );
        BigDecimal scoreDesmatamento = desmatamentoRecente
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
            .min(new BigDecimal("4"));

        return scoreAlertas.add(scoreDesmatamento)
            .max(BigDecimal.ZERO)
            .min(new BigDecimal("10"))
            .setScale(2, RoundingMode.HALF_UP);
    }

    public Semaforo semaforoPorScore(BigDecimal score) {
        BigDecimal safeScore = score == null ? new BigDecimal("50") : score;
        if (safeScore.compareTo(new BigDecimal("70")) >= 0) {
            return Semaforo.VERDE;
        }
        if (safeScore.compareTo(new BigDecimal("40")) >= 0) {
            return Semaforo.AMARELO;
        }
        return Semaforo.VERMELHO;
    }

    private void atualizarMetricasDerivadas(Municipio municipio) {
        BigDecimal score = calcularScorePrioridade(municipio.getId());
        if (score != null) {
            municipio.setScorePrioridade(score);
            if (municipio.getSemaforo() == null) {
                municipio.setSemaforo(semaforoPorScore(score));
            }
        }

        if (municipio.getId() != null) {
            municipio.setKpiRetorno(calcularKpiRetorno(municipio.getId()));
        }
        municipio.setNotaRisco(municipio.getId() == null ? new BigDecimal("0.00") : calcularNotaRisco(municipio.getId()));
    }

    private BigDecimal calcularScorePrioridade(Long municipioId) {
        if (municipioId == null) {
            return new BigDecimal("50.00");
        }

        var indicadorOpt = indicadorRepository.findTopByMunicipioIdOrderByAnoDesc(municipioId);
        if (indicadorOpt.isEmpty()) {
            return null;
        }

        var indicador = indicadorOpt.get();
        List<BigDecimal> valores = new ArrayList<>();
        List<BigDecimal> pesos = new ArrayList<>();

        addFator(valores, pesos, indicador.getDesmatamentoRecenteFactor(), new BigDecimal("40"));
        addFator(valores, pesos, indicador.getEficienciaGastoFactor(), new BigDecimal("30"));
        addFator(valores, pesos, indicador.getIrregularidadesCarFactor(), new BigDecimal("20"));
        addFator(valores, pesos, indicador.getAreaElegivelFactor(), new BigDecimal("10"));

        if (pesos.isEmpty()) {
            return null;
        }

        BigDecimal somaPonderada = BigDecimal.ZERO;
        BigDecimal somaPesos = BigDecimal.ZERO;
        for (int i = 0; i < pesos.size(); i++) {
            somaPonderada = somaPonderada.add(valores.get(i).multiply(pesos.get(i)));
            somaPesos = somaPesos.add(pesos.get(i));
        }

        return somaPonderada.divide(somaPesos, 2, RoundingMode.HALF_UP)
            .max(BigDecimal.ZERO)
            .min(HUNDRED);
    }

    private void addFator(List<BigDecimal> valores, List<BigDecimal> pesos, BigDecimal valor, BigDecimal peso) {
        if (valor == null) {
            return;
        }
        valores.add(valor.max(BigDecimal.ZERO).min(HUNDRED));
        pesos.add(peso);
    }

    private BigDecimal safeKpi(BigDecimal resultadoAmbiental, BigDecimal gastoPublico) {
        if (gastoPublico == null || gastoPublico.compareTo(BigDecimal.ZERO) <= 0 || resultadoAmbiental == null) {
            return null;
        }
        return resultadoAmbiental.divide(gastoPublico, 6, RoundingMode.HALF_UP);
    }

    private JsonNode parseGeoJsonGeometry(String geojson) {
        if (geojson == null || geojson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(geojson);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "geojson_polygon invalido");
        }
    }
}
