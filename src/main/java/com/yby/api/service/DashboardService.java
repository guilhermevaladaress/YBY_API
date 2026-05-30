package com.yby.api.service;

import com.yby.api.dto.DashboardDTO;
import com.yby.api.dto.MunicipioRankingDTO;
import com.yby.api.entity.enums.Gravidade;
import com.yby.api.entity.enums.Semaforo;
import com.yby.api.integration.inpe.InpeQueimadaClient;
import com.yby.api.repository.AlertaRepository;
import com.yby.api.repository.DesmatamentoRepository;
import com.yby.api.repository.IndicadorRepository;
import com.yby.api.repository.MunicipioRepository;
import com.yby.api.service.inteligencia.AlgoritmoMetadata;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final MunicipioRepository municipioRepository;
    private final AlertaRepository alertaRepository;
    private final DesmatamentoRepository desmatamentoRepository;
    private final IndicadorRepository indicadorRepository;
    private final MunicipioService municipioService;
    private final InpeQueimadaClient inpeQueimadaClient;

    public DashboardService(MunicipioRepository municipioRepository,
                            AlertaRepository alertaRepository,
                            DesmatamentoRepository desmatamentoRepository,
                            IndicadorRepository indicadorRepository,
                            MunicipioService municipioService,
                            InpeQueimadaClient inpeQueimadaClient) {
        this.municipioRepository = municipioRepository;
        this.alertaRepository = alertaRepository;
        this.desmatamentoRepository = desmatamentoRepository;
        this.indicadorRepository = indicadorRepository;
        this.municipioService = municipioService;
        this.inpeQueimadaClient = inpeQueimadaClient;
    }

    public DashboardDTO resumo() {
        long totalMunicipios = municipioRepository.count();
        long municipiosVerdes = municipioRepository.findBySemaforo(Semaforo.VERDE).size();
        long municipiosAmarelos = municipioRepository.findBySemaforo(Semaforo.AMARELO).size();
        long municipiosVermelhos = municipioRepository.findBySemaforo(Semaforo.VERMELHO).size();

        long totalAlertasAtivos = alertaRepository.countByAtivoTrue();
        long alertasCriticos = alertaRepository.findByAtivoTrue().stream()
            .filter(a -> a.getGravidade() == Gravidade.ALTA)
            .count();

        int anoAtual = Year.now().getValue();
        BigDecimal totalAreaDesmatada = desmatamentoRepository.findTotalByAno(anoAtual);
        if (totalAreaDesmatada == null) {
            totalAreaDesmatada = desmatamentoRepository.findTotalByAno(anoAtual - 1);
        }

        BigDecimal[] kpiAgregado = calcularKpiAgregado();

        List<MunicipioRankingDTO> top10 = municipioRepository
            .findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "scorePrioridade")))
            .getContent().stream()
            .map(m -> {
                BigDecimal kpi = municipioService.calcularKpiRetorno(m.getId());
                return new MunicipioRankingDTO(
                    m.getId(), m.getNome(), m.getCodigoIbge(),
                    m.getScorePrioridade(), m.getSemaforo().name(), m.getAreaHa(), kpi
                );
            })
            .toList();

        List<MunicipioRankingDTO> top5Desperdicio = municipioRepository
            .findAll(PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "kpiRetorno")))
            .getContent().stream()
            .filter(m -> m.getGastoPublico() != null && m.getGastoPublico().compareTo(BigDecimal.ZERO) > 0)
            .map(m -> {
                BigDecimal kpi = municipioService.calcularKpiRetorno(m.getId());
                return new MunicipioRankingDTO(
                    m.getId(), m.getNome(), m.getCodigoIbge(),
                    m.getScorePrioridade(), m.getSemaforo().name(), m.getAreaHa(), kpi
                );
            })
            .toList();

        Map<String, Long> porBioma = municipioRepository.findAll().stream()
            .filter(m -> m.getBioma() != null)
            .collect(Collectors.groupingBy(m -> m.getBioma().name(), Collectors.counting()));

        long focosAno = inpeQueimadaClient.focosAnoCorrente();
        long focos30dias = inpeQueimadaClient.focosUltimos30Dias();

        return new DashboardDTO(
            totalMunicipios,
            municipiosVerdes,
            municipiosAmarelos,
            municipiosVermelhos,
            kpiAgregado[0],
            kpiAgregado[1],
            kpiAgregado[2],
            totalAreaDesmatada != null ? totalAreaDesmatada : BigDecimal.ZERO,
            totalAlertasAtivos,
            alertasCriticos,
            focosAno,
            focos30dias,
            top10,
            top5Desperdicio,
            porBioma,
            municipioRepository.maxUltimaAtualizacao(),
            AlgoritmoMetadata.VERSAO
        );
    }

    private BigDecimal[] calcularKpiAgregado() {
        int anoAtual = Year.now().getValue();
        List<BigDecimal> kpis = indicadorRepository.findByAno(anoAtual).stream()
            .filter(i -> i.getGastoPublico() != null && i.getGastoPublico().compareTo(BigDecimal.ZERO) > 0
                && i.getResultadoAmbiental() != null)
            .map(i -> i.getResultadoAmbiental().divide(i.getGastoPublico(), 6, RoundingMode.HALF_UP))
            .toList();

        BigDecimal kpiMedio = kpis.isEmpty() ? null :
            kpis.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(kpis.size()), 4, RoundingMode.HALF_UP);

        BigDecimal gastoTotal = indicadorRepository.findByAno(anoAtual).stream()
            .map(i -> i.getGastoPublico() != null ? i.getGastoPublico() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal resultadoTotal = indicadorRepository.findByAno(anoAtual).stream()
            .map(i -> i.getResultadoAmbiental() != null ? i.getResultadoAmbiental() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BigDecimal[]{kpiMedio, gastoTotal, resultadoTotal};
    }
}
