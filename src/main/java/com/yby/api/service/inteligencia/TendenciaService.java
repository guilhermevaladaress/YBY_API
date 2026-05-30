package com.yby.api.service.inteligencia;

import com.yby.api.dto.TendenciaDesmatamentoDTO;
import com.yby.api.entity.enums.StatusTendencia;
import com.yby.api.repository.DesmatamentoRepository;
import com.yby.api.service.MunicipioService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import org.springframework.stereotype.Service;

/**
 * RN-101-A - Score de Prioridade com Tendencia.
 *
 * <p>Captura municipios em aceleracao de desmatamento comparando o ano corrente com o
 * anterior. Ajusta o score base: tendencia &gt; +10% soma 15 pontos (problema piorando);
 * tendencia &lt; -10% subtrai 10 pontos (melhorando); caso contrario nao altera.</p>
 */
@Service
public class TendenciaService {

    /** Limiar de aceleracao/melhora (10%) conforme RN-101-A. */
    static final BigDecimal LIMIAR = new BigDecimal("10");
    static final int AJUSTE_ACELERANDO = 15;
    static final int AJUSTE_MELHORANDO = -10;
    private static final BigDecimal CEM = new BigDecimal("100");

    private final DesmatamentoRepository desmatamentoRepository;
    private final MunicipioService municipioService;

    public TendenciaService(DesmatamentoRepository desmatamentoRepository, MunicipioService municipioService) {
        this.desmatamentoRepository = desmatamentoRepository;
        this.municipioService = municipioService;
    }

    public TendenciaDesmatamentoDTO calcular(Long municipioId, Integer ano) {
        municipioService.buscarMunicipio(municipioId);
        int anoRef = ano == null ? Year.now().getValue() : ano;

        BigDecimal atual = desmatamentoNoAno(municipioId, anoRef);
        BigDecimal anterior = desmatamentoNoAno(municipioId, anoRef - 1);
        return avaliar(municipioId, anoRef, atual, anterior);
    }

    private BigDecimal desmatamentoNoAno(Long municipioId, int ano) {
        BigDecimal soma = desmatamentoRepository.sumAreaByMunicipioAndPeriodo(
            municipioId, LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31));
        return soma == null ? BigDecimal.ZERO : soma;
    }

    /**
     * Calculo puro da tendencia (testavel sem banco). Quando o ano anterior nao tem
     * desmatamento registrado, surgimento de novo desmatamento e tratado como aceleracao
     * e ausencia total como estavel (evita divisao por zero - RN-104).
     */
    TendenciaDesmatamentoDTO avaliar(Long municipioId, int ano, BigDecimal atual, BigDecimal anterior) {
        BigDecimal tendenciaPercent;
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) <= 0) {
            tendenciaPercent = (atual != null && atual.compareTo(BigDecimal.ZERO) > 0)
                ? CEM
                : BigDecimal.ZERO;
        } else {
            tendenciaPercent = atual.subtract(anterior)
                .divide(anterior, 6, RoundingMode.HALF_UP)
                .multiply(CEM)
                .setScale(2, RoundingMode.HALF_UP);
        }

        StatusTendencia status;
        int ajuste;
        if (tendenciaPercent.compareTo(LIMIAR) > 0) {
            status = StatusTendencia.ACELERANDO;
            ajuste = AJUSTE_ACELERANDO;
        } else if (tendenciaPercent.compareTo(LIMIAR.negate()) < 0) {
            status = StatusTendencia.MELHORANDO;
            ajuste = AJUSTE_MELHORANDO;
        } else {
            status = StatusTendencia.ESTAVEL;
            ajuste = 0;
        }

        return new TendenciaDesmatamentoDTO(
            municipioId, ano, atual, anterior, tendenciaPercent, status, ajuste, AlgoritmoMetadata.VERSAO);
    }
}
