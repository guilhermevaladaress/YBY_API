package com.yby.api.service.inteligencia;

import com.yby.api.dto.CarbonoEvitadoDTO;
import com.yby.api.dto.RoiDesmatamentoEvitadoDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * Analise financeira de ROI de Desmatamento Evitado.
 *
 * <p>Compara a receita JREDD+ de manter a floresta (carbono evitado x preco) com o ganho economico
 * de converter a area para uso alternativo (ex.: agropecuaria, R$/ha/ano). Um ROI &gt;= 1 indica que
 * conservar e financeiramente competitivo frente a conversao.</p>
 */
@Service
public class RoiDesmatamentoEvitadoService {

    /** Valor padrao de conversao por hectare/ano quando nao informado (R$/ha/ano). */
    static final BigDecimal VALOR_AGROPECUARIA_PADRAO = new BigDecimal("1200.00");

    private final CarbonoEvitadoService carbonoEvitadoService;

    public RoiDesmatamentoEvitadoService(CarbonoEvitadoService carbonoEvitadoService) {
        this.carbonoEvitadoService = carbonoEvitadoService;
    }

    public RoiDesmatamentoEvitadoDTO avaliar(Long municipioId, Integer ano, BigDecimal preco,
                                             BigDecimal valorAgropecuariaHaAno) {
        CarbonoEvitadoDTO evitado = carbonoEvitadoService.avaliar(municipioId, ano, preco);
        BigDecimal valorConversao = (valorAgropecuariaHaAno == null || valorAgropecuariaHaAno.compareTo(BigDecimal.ZERO) <= 0)
            ? VALOR_AGROPECUARIA_PADRAO : valorAgropecuariaHaAno;
        return montar(evitado, valorConversao);
    }

    /** Nucleo puro do calculo (testavel sem banco). */
    RoiDesmatamentoEvitadoDTO montar(CarbonoEvitadoDTO evitado, BigDecimal valorAgropecuariaHaAno) {
        BigDecimal receitaJredd = evitado.valorPotencialReais();
        BigDecimal ganhoConversao = evitado.hectaresEvitados().multiply(valorAgropecuariaHaAno)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal roi;
        String recomendacao;
        if (evitado.hectaresEvitados().compareTo(BigDecimal.ZERO) <= 0) {
            roi = BigDecimal.ZERO;
            recomendacao = "Sem desmatamento evitado no periodo: nao ha base para comparar ROI.";
        } else if (ganhoConversao.compareTo(BigDecimal.ZERO) <= 0) {
            roi = BigDecimal.ZERO;
            recomendacao = "Ganho de conversao nulo: receita JREDD+ domina a decisao (conservar).";
        } else {
            roi = receitaJredd.divide(ganhoConversao, 4, RoundingMode.HALF_UP);
            recomendacao = roi.compareTo(BigDecimal.ONE) >= 0
                ? "Conservar: a receita JREDD+ supera o ganho economico de conversao da area."
                : "Atencao: a conversao e economicamente mais atrativa; reforcar incentivos/PSA para viabilizar a conservacao.";
        }

        return new RoiDesmatamentoEvitadoDTO(
            evitado.municipioId(), evitado.ano(), evitado.hectaresEvitados(), evitado.tco2eEvitado(),
            evitado.precoTonelada(), receitaJredd, valorAgropecuariaHaAno, ganhoConversao, roi,
            recomendacao, AlgoritmoMetadata.VERSAO);
    }
}
