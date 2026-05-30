package com.yby.api.service;

import com.yby.api.dto.DashboardDTO;
import com.yby.api.entity.enums.Semaforo;
import com.yby.api.repository.MunicipioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final MunicipioRepository municipioRepository;
    private final MunicipioService municipioService;

    public DashboardService(MunicipioRepository municipioRepository, MunicipioService municipioService) {
        this.municipioRepository = municipioRepository;
        this.municipioService = municipioService;
    }

    public DashboardDTO carregar() {
        long totalMunicipios = municipioRepository.count();
        long municipiosCriticos = municipioRepository.findAll()
            .stream()
            .filter(m -> m.getSemaforo() == Semaforo.VERMELHO)
            .count();

        var municipios = municipioRepository.findAll();
        BigDecimal somaKpi = BigDecimal.ZERO;
        int qtdComKpi = 0;
        for (var municipio : municipios) {
            BigDecimal kpi = municipioService.calcularKpiRetorno(municipio.getId());
            if (kpi != null) {
                somaKpi = somaKpi.add(kpi);
                qtdComKpi++;
            }
        }
        BigDecimal kpiMedio = qtdComKpi == 0
            ? BigDecimal.ZERO
            : somaKpi.divide(BigDecimal.valueOf(qtdComKpi), 6, RoundingMode.HALF_UP);

        return new DashboardDTO(
            totalMunicipios,
            municipiosCriticos,
            kpiMedio,
            municipioService.ranking(0, 5, "score", "desc").getContent()
        );
    }
}
