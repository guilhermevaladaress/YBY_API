package com.yby.api.service.inteligencia;

import com.yby.api.dto.KpiMultidimensionalDTO;
import com.yby.api.entity.Indicador;
import com.yby.api.exception.BusinessException;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.repository.IndicadorRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * RN-103-B - KPI Multidimensional.
 *
 * <p>Incorpora impacto social e eficiencia fiscal ao resultado ambiental. As tres dimensoes
 * sao normalizadas para 0-100 frente ao maior valor estadual do ano (benchmarking justo) e
 * combinadas com pesos. Investimento social relevante gera bonus de 5% a 15%.</p>
 *
 * <p>Base legal do componente social: Lei 14.119/2021 (Politica Nacional de PSA) e as
 * salvaguardas de Cancun (envolvimento de comunidades tradicionais em projetos REDD+).</p>
 */
@Service
public class KpiMultidimensionalService {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal PESO_AMBIENTAL_PADRAO = new BigDecimal("0.50");
    private static final BigDecimal PESO_SOCIAL_PADRAO = new BigDecimal("0.25");
    private static final BigDecimal PESO_FISCAL_PADRAO = new BigDecimal("0.25");
    private static final BigDecimal BONUS_MIN = new BigDecimal("5");
    private static final BigDecimal BONUS_MAX = new BigDecimal("15");
    /** Investimento social >= 20% do gasto atinge o bonus maximo. */
    private static final BigDecimal SHARE_BONUS_MAX = new BigDecimal("0.20");

    private final IndicadorRepository indicadorRepository;

    public KpiMultidimensionalService(IndicadorRepository indicadorRepository) {
        this.indicadorRepository = indicadorRepository;
    }

    public KpiMultidimensionalDTO calcular(Long municipioId, Integer ano,
                                           BigDecimal pesoAmbiental, BigDecimal pesoSocial, BigDecimal pesoFiscal) {
        List<Indicador> doAno = indicadorRepository.findByAno(ano);
        Indicador alvo = doAno.stream()
            .filter(i -> i.getMunicipio().getId().equals(municipioId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(
                "Indicador nao encontrado para municipio " + municipioId + " no ano " + ano));

        BigDecimal maxAmbiental = max(doAno, Indicador::getResultadoAmbiental);
        BigDecimal maxKpi = doAno.stream().map(this::kpiRetorno).reduce(BigDecimal.ZERO, BigDecimal::max);
        BigDecimal maxEmpregos = max(doAno, i -> intToBd(i.getEmpregosConservacao()));
        BigDecimal maxFamilias = max(doAno, i -> intToBd(i.getFamiliasPsa()));
        BigDecimal maxComunidades = max(doAno, i -> intToBd(i.getComunidadesTradicionais()));

        BigDecimal ambiental = normalizar(alvo.getResultadoAmbiental(), maxAmbiental);
        BigDecimal fiscal = normalizar(kpiRetorno(alvo), maxKpi);
        BigDecimal social = media(
            normalizar(intToBd(alvo.getEmpregosConservacao()), maxEmpregos),
            normalizar(intToBd(alvo.getFamiliasPsa()), maxFamilias),
            normalizar(intToBd(alvo.getComunidadesTradicionais()), maxComunidades));

        BigDecimal bonus = bonusSocial(alvo.getInvestimentoSocial(), alvo.getGastoPublico());

        return montar(municipioId, ano, ambiental, social, fiscal,
            pesoAmbiental, pesoSocial, pesoFiscal, bonus);
    }

    /** Nucleo puro do calculo (testavel sem banco). Pesos sao reponderados para somar 1. */
    KpiMultidimensionalDTO montar(Long municipioId, Integer ano,
                                  BigDecimal ambiental, BigDecimal social, BigDecimal fiscal,
                                  BigDecimal pesoAmbiental, BigDecimal pesoSocial, BigDecimal pesoFiscal,
                                  BigDecimal bonusPercentual) {
        BigDecimal wa = padrao(pesoAmbiental, PESO_AMBIENTAL_PADRAO);
        BigDecimal ws = padrao(pesoSocial, PESO_SOCIAL_PADRAO);
        BigDecimal wf = padrao(pesoFiscal, PESO_FISCAL_PADRAO);
        BigDecimal somaPesos = wa.add(ws).add(wf);
        if (somaPesos.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "Soma dos pesos deve ser positiva");
        }

        BigDecimal kpiBase = ambiental.multiply(wa)
            .add(social.multiply(ws))
            .add(fiscal.multiply(wf))
            .divide(somaPesos, 4, RoundingMode.HALF_UP);

        BigDecimal bonus = bonusPercentual == null ? BigDecimal.ZERO : bonusPercentual;
        BigDecimal kpiFinal = kpiBase
            .multiply(BigDecimal.ONE.add(bonus.divide(CEM, 6, RoundingMode.HALF_UP)))
            .setScale(2, RoundingMode.HALF_UP);

        return new KpiMultidimensionalDTO(
            municipioId, ano,
            scale2(ambiental), scale2(social), scale2(fiscal),
            wa, ws, wf, scale2(bonus), kpiBase.setScale(2, RoundingMode.HALF_UP), kpiFinal,
            AlgoritmoMetadata.VERSAO);
    }

    BigDecimal bonusSocial(BigDecimal investimentoSocial, BigDecimal gastoPublico) {
        if (investimentoSocial == null || investimentoSocial.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (gastoPublico == null || gastoPublico.compareTo(BigDecimal.ZERO) <= 0) {
            // Ha investimento social mas sem gasto base comparavel: bonus intermediario.
            return BONUS_MIN.add(BONUS_MAX).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        }
        BigDecimal share = investimentoSocial.divide(gastoPublico, 6, RoundingMode.HALF_UP);
        BigDecimal escalonado = BONUS_MIN.add(
            share.divide(SHARE_BONUS_MAX, 6, RoundingMode.HALF_UP).multiply(BONUS_MAX.subtract(BONUS_MIN)));
        return escalonado.max(BONUS_MIN).min(BONUS_MAX).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal kpiRetorno(Indicador i) {
        if (i.getGastoPublico() == null || i.getGastoPublico().compareTo(BigDecimal.ZERO) <= 0
            || i.getResultadoAmbiental() == null) {
            return BigDecimal.ZERO;
        }
        return i.getResultadoAmbiental().divide(i.getGastoPublico(), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizar(BigDecimal valor, BigDecimal max) {
        if (valor == null || max == null || max.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return valor.divide(max, 6, RoundingMode.HALF_UP).multiply(CEM).max(BigDecimal.ZERO).min(CEM);
    }

    private BigDecimal media(BigDecimal... valores) {
        BigDecimal soma = BigDecimal.ZERO;
        for (BigDecimal v : valores) {
            soma = soma.add(v);
        }
        return soma.divide(BigDecimal.valueOf(valores.length), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal max(List<Indicador> lista, java.util.function.Function<Indicador, BigDecimal> extrator) {
        return lista.stream()
            .map(extrator)
            .filter(v -> v != null)
            .reduce(BigDecimal.ZERO, BigDecimal::max);
    }

    private BigDecimal intToBd(Integer valor) {
        return valor == null ? BigDecimal.ZERO : BigDecimal.valueOf(valor);
    }

    private BigDecimal padrao(BigDecimal valor, BigDecimal padrao) {
        return (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) ? padrao : valor;
    }

    private BigDecimal scale2(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
