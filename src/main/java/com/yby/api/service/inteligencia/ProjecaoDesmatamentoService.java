package com.yby.api.service.inteligencia;

import com.yby.api.dto.ProjecaoDesmatamentoDTO;
import com.yby.api.dto.ValorAnualDTO;
import com.yby.api.repository.DesmatamentoRepository;
import com.yby.api.service.MunicipioService;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Analise de Projecao de Desmatamento.
 *
 * <p>Projeta a area desmatada (ha) dos proximos anos a partir da tendencia historica, usando a
 * taxa media de crescimento composta (CAGR) entre o primeiro e o ultimo ano com dado. Apoia a
 * definicao da linha de base do JREDD+. Nunca divide por zero (RN-104): series vazias ou sem
 * crescimento estimavel projetam o ultimo valor observado.</p>
 */
@Service
public class ProjecaoDesmatamentoService {

    static final int ANOS_HISTORICO = 6;
    static final int HORIZONTE_PADRAO = 5;
    private static final BigDecimal CEM = new BigDecimal("100");

    private final MunicipioService municipioService;
    private final DesmatamentoRepository desmatamentoRepository;

    public ProjecaoDesmatamentoService(MunicipioService municipioService,
                                       DesmatamentoRepository desmatamentoRepository) {
        this.municipioService = municipioService;
        this.desmatamentoRepository = desmatamentoRepository;
    }

    public ProjecaoDesmatamentoDTO avaliar(Long municipioId, Integer horizonteAnos) {
        municipioService.buscarMunicipio(municipioId);
        int horizonte = (horizonteAnos == null || horizonteAnos <= 0) ? HORIZONTE_PADRAO : horizonteAnos;
        // A serie PRODES e anual e defasada: ancora no ultimo ano COM dado (nunca no ano corrente
        // vazio, que produziria projecao zerada). Fallback para o ano corrente quando nao ha serie.
        Integer anoMax = desmatamentoRepository.maxAno();
        int anoAtual = (anoMax == null || anoMax == 0) ? Year.now().getValue() : anoMax;

        List<ValorAnualDTO> historico = new ArrayList<>();
        for (int a = anoAtual - (ANOS_HISTORICO - 1); a <= anoAtual; a++) {
            historico.add(new ValorAnualDTO(a, areaNoAno(municipioId, a)));
        }
        return calcular(municipioId, historico, horizonte);
    }

    /** Nucleo puro do calculo (testavel sem banco). */
    ProjecaoDesmatamentoDTO calcular(Long municipioId, List<ValorAnualDTO> historico, int horizonte) {
        if (historico.isEmpty()) {
            return new ProjecaoDesmatamentoDTO(municipioId, null, horizonte, BigDecimal.ZERO,
                BigDecimal.ZERO, historico, List.of(), AlgoritmoMetadata.VERSAO);
        }

        int anoBase = historico.getLast().ano();
        BigDecimal ultimoValor = historico.getLast().valor();
        BigDecimal media = historico.stream().map(ValorAnualDTO::valor)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(historico.size()), 2, RoundingMode.HALF_UP);

        BigDecimal taxa = estimarCagr(historico);

        List<ValorAnualDTO> projecao = new ArrayList<>();
        BigDecimal fatorBase = BigDecimal.ONE.add(taxa);
        for (int k = 1; k <= horizonte; k++) {
            BigDecimal projetado = ultimoValor.multiply(fatorBase.pow(k))
                .max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            projecao.add(new ValorAnualDTO(anoBase + k, projetado));
        }

        BigDecimal taxaPercent = taxa.multiply(CEM).setScale(2, RoundingMode.HALF_UP);
        return new ProjecaoDesmatamentoDTO(municipioId, anoBase, horizonte, taxaPercent, media,
            historico, projecao, AlgoritmoMetadata.VERSAO);
    }

    /** CAGR entre o primeiro ano com valor positivo e o ultimo; 0 quando indeterminavel. */
    private BigDecimal estimarCagr(List<ValorAnualDTO> historico) {
        int inicio = -1;
        for (int i = 0; i < historico.size(); i++) {
            if (historico.get(i).valor() != null && historico.get(i).valor().compareTo(BigDecimal.ZERO) > 0) {
                inicio = i;
                break;
            }
        }
        if (inicio < 0 || inicio == historico.size() - 1) {
            return BigDecimal.ZERO;
        }
        BigDecimal primeiro = historico.get(inicio).valor();
        BigDecimal ultimo = historico.getLast().valor();
        if (ultimo.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        int periodos = (historico.size() - 1) - inicio;
        double razao = ultimo.divide(primeiro, 10, RoundingMode.HALF_UP).doubleValue();
        double cagr = Math.pow(razao, 1.0 / periodos) - 1.0;
        return new BigDecimal(cagr, new MathContext(8)).setScale(6, RoundingMode.HALF_UP);
    }

    private BigDecimal areaNoAno(Long municipioId, int ano) {
        BigDecimal area = desmatamentoRepository.sumAreaByMunicipioAndPeriodo(
            municipioId, LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31));
        return area == null ? BigDecimal.ZERO : area;
    }
}
