package com.yby.api.service;

import com.yby.api.dto.MunicipioDTO;
import com.yby.api.dto.MunicipioDetalheDTO;
import com.yby.api.dto.MunicipioRankingDTO;
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
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MunicipioService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final MunicipioRepository municipioRepository;
    private final IndicadorRepository indicadorRepository;
    private final DesmatamentoRepository desmatamentoRepository;
    private final AlertaRepository alertaRepository;
    private final AuditService auditService;

    public MunicipioService(
        MunicipioRepository municipioRepository,
        IndicadorRepository indicadorRepository,
        DesmatamentoRepository desmatamentoRepository,
        AlertaRepository alertaRepository,
        AuditService auditService
    ) {
        this.municipioRepository = municipioRepository;
        this.indicadorRepository = indicadorRepository;
        this.desmatamentoRepository = desmatamentoRepository;
        this.alertaRepository = alertaRepository;
        this.auditService = auditService;
    }

    public Page<MunicipioDTO> listar(Pageable pageable) {
        Page<Municipio> page = municipioRepository.findAll(pageable);
        return page.map(municipio -> MunicipioMapper.toDto(municipio, calcularKpiRetorno(municipio.getId())));
    }

    public Page<MunicipioRankingDTO> ranking(int page, int size, String ordenar, String ordem) {
        Sort.Direction direction = "asc".equalsIgnoreCase(ordem) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String campo = switch (ordenar == null ? "score" : ordenar.toLowerCase()) {
            case "nome" -> "nome";
            case "area" -> "areaHa";
            default -> "scorePrioridade";
        };
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, campo));
        Page<Municipio> municipios = municipioRepository.findAll(pageRequest);
        List<MunicipioRankingDTO> ranking = municipios.getContent()
            .stream()
            .map(municipio -> MunicipioMapper.toRankingDto(municipio, calcularKpiRetorno(municipio.getId())))
            .toList();
        return new PageImpl<>(ranking, pageRequest, municipios.getTotalElements());
    }

    public MunicipioDetalheDTO buscarDetalhe(Long id) {
        Municipio municipio = buscarMunicipio(id);
        var risco = calcularNotaRisco(municipio.getId());
        List<String> pendencias = alertaRepository.findByMunicipioIdAndResolvidoFalseOrderByCreatedAtDesc(municipio.getId())
            .stream()
            .map(alerta -> alerta.getTipo().name() + ": " + alerta.getDescricao())
            .toList();
        return MunicipioMapper.toDetalheDto(municipio, risco, calcularKpiRetorno(id), pendencias);
    }

    @Transactional
    public MunicipioDTO criar(MunicipioUpsertDTO dto) {
        if (municipioRepository.existsByCodigoIbge(dto.codigoIbge())) {
            throw new BusinessException("Ja existe municipio com este codigo IBGE");
        }
        Municipio municipio = new Municipio();
        aplicarDados(municipio, dto);
        Municipio saved = municipioRepository.save(municipio);
        auditService.registrarEscritaGestor("CREATE", "municipios", String.valueOf(saved.getId()), dto);
        return MunicipioMapper.toDto(saved, calcularKpiRetorno(saved.getId()));
    }

    @Transactional
    public MunicipioDTO atualizar(Long id, MunicipioUpsertDTO dto) {
        Municipio municipio = buscarMunicipio(id);

        if (!municipio.getCodigoIbge().equals(dto.codigoIbge()) && municipioRepository.existsByCodigoIbge(dto.codigoIbge())) {
            throw new BusinessException("Ja existe municipio com este codigo IBGE");
        }

        aplicarDados(municipio, dto);
        Municipio saved = municipioRepository.save(municipio);
        auditService.registrarEscritaGestor("UPDATE", "municipios", String.valueOf(saved.getId()), dto);
        return MunicipioMapper.toDto(saved, calcularKpiRetorno(saved.getId()));
    }

    @Transactional
    public void deletar(Long id) {
        Municipio municipio = buscarMunicipio(id);
        municipioRepository.delete(municipio);
        auditService.registrarEscritaGestor("DELETE", "municipios", String.valueOf(id), municipio.getNome());
    }

    public Municipio buscarMunicipio(Long id) {
        return municipioRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Municipio nao encontrado"));
    }

    public BigDecimal calcularKpiRetorno(Long municipioId) {
        return indicadorRepository.findTopByMunicipioIdOrderByAnoDesc(municipioId)
            .map(indicador -> {
                if (indicador.getGastoPublico() == null || indicador.getGastoPublico().compareTo(BigDecimal.ZERO) <= 0) {
                    return null;
                }
                if (indicador.getResultadoAmbiental() == null) {
                    return null;
                }
                return indicador.getResultadoAmbiental()
                    .divide(indicador.getGastoPublico(), 6, RoundingMode.HALF_UP);
            })
            .orElse(null);
    }

    public BigDecimal calcularNotaRisco(Long municipioId) {
        var alertas = alertaRepository.findByMunicipioIdAndResolvidoFalseOrderByCreatedAtDesc(municipioId);
        BigDecimal scoreAlertas = BigDecimal.valueOf(
            alertas.stream()
                .mapToInt(alerta -> switch (alerta.getGravidade()) {
                    case ALTA -> 3;
                    case MEDIA -> 2;
                    case BAIXA -> 1;
                })
                .sum()
        );

        BigDecimal areaRecente = desmatamentoRepository.sumAreaByMunicipioAndPeriodo(
            municipioId,
            LocalDate.now().minusMonths(12),
            LocalDate.now()
        );

        BigDecimal scoreDesmatamento = areaRecente
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP)
            .min(new BigDecimal("4"));

        BigDecimal total = scoreAlertas.add(scoreDesmatamento).min(new BigDecimal("10"));
        return total.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private void aplicarDados(Municipio municipio, MunicipioUpsertDTO dto) {
        municipio.setNome(dto.nome().trim());
        municipio.setCodigoIbge(dto.codigoIbge().trim());
        municipio.setAreaHa(dto.areaHa());
        municipio.setGeojsonPolygon(dto.geojsonPolygon());

        BigDecimal score = dto.scorePrioridade() != null
            ? limitarEntreZeroECem(dto.scorePrioridade())
            : estimarScore(municipio);
        municipio.setScorePrioridade(score);
        municipio.setSemaforo(dto.semaforo() != null ? dto.semaforo() : semaforoPorScore(score));
    }

    private BigDecimal estimarScore(Municipio municipio) {
        if (municipio.getId() == null) {
            return new BigDecimal("50.00");
        }

        BigDecimal somaPonderada = BigDecimal.ZERO;
        BigDecimal somaPesos = BigDecimal.ZERO;
        List<BigDecimal> valores = new ArrayList<>();
        List<BigDecimal> pesos = new ArrayList<>();

        BigDecimal desmatamentoRecente = desmatamentoRepository.sumAreaByMunicipioAndPeriodo(
            municipio.getId(),
            LocalDate.now().minusMonths(12),
            LocalDate.now()
        );
        if (desmatamentoRecente != null) {
            BigDecimal fatorDesmatamento = HUNDRED.subtract(
                desmatamentoRecente.min(new BigDecimal("1000"))
                    .divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP)
            );
            valores.add(limitarEntreZeroECem(fatorDesmatamento));
            pesos.add(new BigDecimal("40"));
        }

        indicadorRepository.findTopByMunicipioIdOrderByAnoDesc(municipio.getId()).ifPresent(indicador -> {
            if (indicador.getEficienciaGasto() != null) {
                valores.add(limitarEntreZeroECem(indicador.getEficienciaGasto()));
                pesos.add(new BigDecimal("30"));
            }
            if (indicador.getIrregularidadesCar() != null) {
                BigDecimal fatorCar = HUNDRED.subtract(new BigDecimal(indicador.getIrregularidadesCar() * 5L));
                valores.add(limitarEntreZeroECem(fatorCar));
                pesos.add(new BigDecimal("20"));
            }
            if (indicador.getAreaElegivelHa() != null) {
                BigDecimal fatorArea = indicador.getAreaElegivelHa()
                    .divide(new BigDecimal("1000"), 6, RoundingMode.HALF_UP)
                    .multiply(HUNDRED);
                valores.add(limitarEntreZeroECem(fatorArea));
                pesos.add(new BigDecimal("10"));
            }
        });

        for (int i = 0; i < valores.size(); i++) {
            somaPonderada = somaPonderada.add(valores.get(i).multiply(pesos.get(i)));
            somaPesos = somaPesos.add(pesos.get(i));
        }
        if (somaPesos.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("50.00");
        }
        return somaPonderada.divide(somaPesos, 2, RoundingMode.HALF_UP);
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

    private BigDecimal limitarEntreZeroECem(BigDecimal valor) {
        if (valor == null) {
            return null;
        }
        return valor.max(BigDecimal.ZERO).min(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }
}
