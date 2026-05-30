package com.yby.api.service;

import com.yby.api.dto.EstatisticaDashboardDTO;
import com.yby.api.dto.EstatisticaGeralDTO;
import com.yby.api.repository.AlertaRepository;
import com.yby.api.repository.DesmatamentoRepository;
import com.yby.api.repository.IndicadorRepository;
import com.yby.api.repository.MunicipioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class EstatisticaService {

    private final MunicipioRepository municipioRepository;
    private final AlertaRepository alertaRepository;
    private final DesmatamentoRepository desmatamentoRepository;
    private final IndicadorRepository indicadorRepository;
    private final MunicipioService municipioService;
    private final SyncService syncService;

    public EstatisticaService(
        MunicipioRepository municipioRepository,
        AlertaRepository alertaRepository,
        DesmatamentoRepository desmatamentoRepository,
        IndicadorRepository indicadorRepository,
        MunicipioService municipioService,
        SyncService syncService
    ) {
        this.municipioRepository = municipioRepository;
        this.alertaRepository = alertaRepository;
        this.desmatamentoRepository = desmatamentoRepository;
        this.indicadorRepository = indicadorRepository;
        this.municipioService = municipioService;
        this.syncService = syncService;
    }

    public EstatisticaGeralDTO geral() {
        long totalMunicipios = municipioRepository.count();
        long totalAlertasAtivos = alertaRepository.countByResolvidoFalse();

        BigDecimal areaDesmatada = desmatamentoRepository.findByDataReferenciaBetween(
            LocalDate.now().minusMonths(12),
            LocalDate.now()
        )
            .stream()
            .map(item -> item.getAreaDesmatadaHa() == null ? BigDecimal.ZERO : item.getAreaDesmatadaHa())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gastoPublicoTotal = indicadorRepository.findAll()
            .stream()
            .map(indicador -> indicador.getGastoPublico() == null ? BigDecimal.ZERO : indicador.getGastoPublico())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal resultadoAmbientalTotal = indicadorRepository.findAll()
            .stream()
            .map(indicador -> indicador.getResultadoAmbiental() == null ? BigDecimal.ZERO : indicador.getResultadoAmbiental())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal kpiMedio = BigDecimal.ZERO;
        if (gastoPublicoTotal.compareTo(BigDecimal.ZERO) > 0) {
            kpiMedio = resultadoAmbientalTotal.divide(gastoPublicoTotal, 6, RoundingMode.HALF_UP);
        }

        return new EstatisticaGeralDTO(
            totalMunicipios,
            totalAlertasAtivos,
            areaDesmatada,
            gastoPublicoTotal,
            resultadoAmbientalTotal,
            kpiMedio
        );
    }

    public EstatisticaDashboardDTO dashboard() {
        long totalMunicipios = municipioRepository.count();
        long totalAlertasAtivos = alertaRepository.countByResolvidoFalse();
        long totalJobsSync = syncService.totalJobs();

        BigDecimal scoreMedio = municipioRepository.findAll()
            .stream()
            .map(m -> m.getScorePrioridade() == null ? BigDecimal.ZERO : m.getScorePrioridade())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalMunicipios > 0) {
            scoreMedio = scoreMedio.divide(BigDecimal.valueOf(totalMunicipios), 2, RoundingMode.HALF_UP);
        }

        var ranking = municipioService.ranking(0, 5, "score", "desc").getContent();
        return new EstatisticaDashboardDTO(
            totalMunicipios,
            totalAlertasAtivos,
            totalJobsSync,
            scoreMedio,
            ranking
        );
    }
}
