package com.yby.api.service;

import com.yby.api.dto.AlertaDTO;
import com.yby.api.dto.RiscoDTO;
import com.yby.api.entity.Alerta;
import com.yby.api.entity.Indicador;
import com.yby.api.entity.enums.AlertaTipo;
import com.yby.api.entity.enums.Gravidade;
import com.yby.api.exception.BusinessException;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.mapper.AlertaMapper;
import com.yby.api.repository.AlertaRepository;
import com.yby.api.repository.IndicadorRepository;
import java.math.BigDecimal;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final IndicadorRepository indicadorRepository;
    private final MunicipioService municipioService;
    private final AlertaMapper alertaMapper;
    private final AuditService auditService;

    public AlertaService(AlertaRepository alertaRepository,
                         IndicadorRepository indicadorRepository,
                         MunicipioService municipioService,
                         AlertaMapper alertaMapper,
                         AuditService auditService) {
        this.alertaRepository = alertaRepository;
        this.indicadorRepository = indicadorRepository;
        this.municipioService = municipioService;
        this.alertaMapper = alertaMapper;
        this.auditService = auditService;
    }

    public List<AlertaDTO> desperdicio(Integer ano, Integer limite) {
        int anoConsulta = ano == null ? Year.now().getValue() : ano;
        int limiteConsulta = (limite == null || limite <= 0) ? 10 : limite;

        List<Indicador> indicadoresAno = indicadorRepository.findByAno(anoConsulta);
        if (indicadoresAno.isEmpty()) {
            return List.of();
        }

        BigDecimal medianaGasto = mediana(indicadoresAno.stream()
            .map(Indicador::getGastoPublico)
            .filter(v -> v != null)
            .toList());

        BigDecimal medianaResultado = mediana(indicadoresAno.stream()
            .map(Indicador::getResultadoAmbiental)
            .filter(v -> v != null)
            .toList());

        return indicadoresAno.stream()
            .filter(i -> i.getGastoPublico() != null && i.getResultadoAmbiental() != null)
            .filter(i -> i.getGastoPublico().compareTo(medianaGasto) > 0)
            .filter(i -> i.getResultadoAmbiental().compareTo(medianaResultado) < 0)
            .sorted(Comparator.comparing(Indicador::getGastoPublico).reversed())
            .limit(limiteConsulta)
            .map(i -> new AlertaDTO(
                null,
                i.getMunicipio().getId(),
                AlertaTipo.MANUAL.name(),
                Gravidade.ALTA.name(),
                "Alerta de desperdicio: gasto acima da mediana e resultado ambiental abaixo da mediana",
                "Revisar alocacao orcamentaria e plano de execucao",
                null,
                true
            ))
            .toList();
    }

    public RiscoDTO risco(Long municipioId) {
        municipioService.buscarMunicipio(municipioId);

        List<Alerta> alertas = alertaRepository.findByMunicipioIdAndAtivoTrueOrderByCreatedAtDesc(municipioId);
        List<String> pendencias = alertas.stream()
            .map(a -> a.getTipo().name() + " - " + a.getDescricao() + " | acao: " + a.getAcaoRecomendada())
            .toList();

        BigDecimal nota = calcularNotaRisco(alertas);
        String semaforo = nota.compareTo(new BigDecimal("6.00")) >= 0
            ? "vermelho"
            : nota.compareTo(new BigDecimal("3.00")) >= 0 ? "amarelo" : "verde";

        return new RiscoDTO(municipioId, nota, semaforo, pendencias);
    }

    @Transactional
    public AlertaDTO upsert(AlertaDTO dto) {
        var municipio = municipioService.buscarMunicipio(dto.municipioId());

        Alerta alerta = dto.id() == null
            ? new Alerta()
            : alertaRepository.findById(dto.id())
                .orElseThrow(() -> new ResourceNotFoundException("Alerta nao encontrado"));

        alerta.setMunicipio(municipio);
        alerta.setTipo(parseTipo(dto.tipo()));
        alerta.setGravidade(parseGravidade(dto.gravidade()));
        alerta.setDescricao(dto.descricao());
        alerta.setAcaoRecomendada(dto.acaoRecomendada());
        alerta.setDataAlerta(dto.dataAlerta());
        alerta.setAtivo(dto.ativo() == null || dto.ativo());

        Alerta saved = alertaRepository.save(alerta);
        auditService.registrarEscritaGestor("UPSERT", "alertas", String.valueOf(saved.getId()), dto);
        return alertaMapper.toDTO(saved);
    }

    private BigDecimal mediana(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> ordenados = new ArrayList<>(valores);
        ordenados.sort(BigDecimal::compareTo);

        int tamanho = ordenados.size();
        if (tamanho % 2 == 1) {
            return ordenados.get(tamanho / 2);
        }

        BigDecimal a = ordenados.get((tamanho / 2) - 1);
        BigDecimal b = ordenados.get(tamanho / 2);
        return a.add(b).divide(new BigDecimal("2"), 2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal calcularNotaRisco(List<Alerta> alertas) {
        BigDecimal nota = BigDecimal.ZERO;
        for (Alerta alerta : alertas) {
            BigDecimal tipoPeso = switch (alerta.getTipo()) {
                case EMBARGO -> new BigDecimal("3.0");
                case SOBREPOSICAO -> new BigDecimal("2.5");
                case CAR_IRREGULAR -> new BigDecimal("2.0");
                case MANUAL -> new BigDecimal("1.0");
            };
            BigDecimal gravidadePeso = switch (alerta.getGravidade()) {
                case ALTA -> new BigDecimal("1.0");
                case MEDIA -> new BigDecimal("0.6");
                case BAIXA -> new BigDecimal("0.3");
            };
            nota = nota.add(tipoPeso.multiply(gravidadePeso));
        }

        return nota.min(new BigDecimal("10.0"))
            .max(BigDecimal.ZERO)
            .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private AlertaTipo parseTipo(String tipo) {
        try {
            return AlertaTipo.valueOf(tipo.toUpperCase());
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "tipo de alerta invalido");
        }
    }

    private Gravidade parseGravidade(String gravidade) {
        try {
            return Gravidade.valueOf(gravidade.toUpperCase());
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "gravidade invalida");
        }
    }
}
