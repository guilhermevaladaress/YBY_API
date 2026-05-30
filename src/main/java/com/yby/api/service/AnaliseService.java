package com.yby.api.service;

import com.yby.api.dto.ProntidaoDTO;
import com.yby.api.dto.RetornoDTO;
import com.yby.api.dto.RiscoDTO;
import com.yby.api.entity.enums.Semaforo;
import com.yby.api.repository.AlertaRepository;
import com.yby.api.repository.IndicadorRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnaliseService {

    private final MunicipioService municipioService;
    private final AlertaRepository alertaRepository;
    private final IndicadorRepository indicadorRepository;

    public AnaliseService(
        MunicipioService municipioService,
        AlertaRepository alertaRepository,
        IndicadorRepository indicadorRepository
    ) {
        this.municipioService = municipioService;
        this.alertaRepository = alertaRepository;
        this.indicadorRepository = indicadorRepository;
    }

    public RiscoDTO risco(Long municipioId) {
        municipioService.buscarMunicipio(municipioId);
        BigDecimal notaRisco = municipioService.calcularNotaRisco(municipioId);
        List<String> pendencias = alertaRepository.findByMunicipioIdAndResolvidoFalseOrderByCreatedAtDesc(municipioId)
            .stream()
            .map(alerta -> alerta.getTipo().name() + " - " + alerta.getDescricao())
            .toList();

        Semaforo semaforo = notaRisco.compareTo(new BigDecimal("6.00")) > 0
            ? Semaforo.VERMELHO
            : notaRisco.compareTo(new BigDecimal("3.00")) > 0 ? Semaforo.AMARELO : Semaforo.VERDE;
        return new RiscoDTO(municipioId, notaRisco, semaforo, pendencias);
    }

    public ProntidaoDTO prontidao(Long municipioId) {
        RiscoDTO risco = risco(municipioId);
        List<String> motivos = new ArrayList<>();
        Semaforo prontidao;

        boolean possuiEmbargoOuSobreposicaoCritica = risco.pendencias()
            .stream()
            .map(String::toUpperCase)
            .anyMatch(item -> item.contains("EMBARGO") || item.contains("SOBREPOSICAO"));

        if (possuiEmbargoOuSobreposicaoCritica || risco.notaRisco().compareTo(new BigDecimal("7")) >= 0) {
            prontidao = Semaforo.VERMELHO;
            motivos.add("Alto risco com bloqueio ambiental/legal relevante");
        } else if (risco.notaRisco().compareTo(new BigDecimal("4")) >= 0) {
            prontidao = Semaforo.AMARELO;
            motivos.add("Pendencias resolviveis identificadas");
        } else {
            prontidao = Semaforo.VERDE;
            motivos.add("Baixo risco e municipio apto para investimento");
        }

        if (risco.pendencias().isEmpty()) {
            motivos.add("Sem pendencias abertas");
        } else {
            motivos.add("Pendencias abertas: " + risco.pendencias().size());
        }

        return new ProntidaoDTO(municipioId, prontidao, motivos);
    }

    public RetornoDTO retorno(Long municipioId) {
        municipioService.buscarMunicipio(municipioId);
        var indicadorOpt = indicadorRepository.findTopByMunicipioIdOrderByAnoDesc(municipioId);

        BigDecimal gastoPublico = indicadorOpt.map(i -> i.getGastoPublico()).orElse(null);
        BigDecimal resultadoAmbiental = indicadorOpt.map(i -> i.getResultadoAmbiental()).orElse(null);
        BigDecimal kpiRetorno = null;

        if (gastoPublico != null && gastoPublico.compareTo(BigDecimal.ZERO) > 0 && resultadoAmbiental != null) {
            kpiRetorno = resultadoAmbiental.divide(gastoPublico, 6, RoundingMode.HALF_UP);
        }

        String classificacao = "SEM_DADOS";
        if (kpiRetorno != null) {
            classificacao = kpiRetorno.compareTo(BigDecimal.ONE) >= 0
                ? "ALTO_RETORNO"
                : kpiRetorno.compareTo(new BigDecimal("0.50")) >= 0 ? "RETORNO_MODERADO" : "RETORNO_BAIXO";
        }

        return new RetornoDTO(municipioId, gastoPublico, resultadoAmbiental, kpiRetorno, classificacao);
    }
}
