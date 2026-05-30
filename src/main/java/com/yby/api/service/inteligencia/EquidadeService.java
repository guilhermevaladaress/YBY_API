package com.yby.api.service.inteligencia;

import com.yby.api.dto.EquidadeTerritorialDTO;
import com.yby.api.entity.Indicador;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RN-500 - Equidade Territorial.
 *
 * <p>Evita concentracao do investimento em poucos municipios. Calcula o indice de equidade
 * {@code 1 - (desvio/media)} sobre o gasto publico do ano; quando inferior a 0,3, sinaliza
 * concentracao elevada e indica os municipios abaixo da mediana como elegiveis a bonus de score.</p>
 */
@Service
public class EquidadeService {

    /** Limiar de concentracao elevada conforme RN-500. */
    static final BigDecimal LIMIAR_CONCENTRACAO = new BigDecimal("0.3");
    /** Bonus de score sugerido aos municipios abaixo da mediana quando ha concentracao. */
    static final int BONUS_SCORE = 10;

    private final com.yby.api.repository.IndicadorRepository indicadorRepository;

    public EquidadeService(com.yby.api.repository.IndicadorRepository indicadorRepository) {
        this.indicadorRepository = indicadorRepository;
    }

    @Transactional(readOnly = true)
    public EquidadeTerritorialDTO avaliar(Integer ano) {
        int anoRef = ano == null ? Year.now().getValue() : ano;
        List<Indicador> indicadores = indicadorRepository.findByAno(anoRef).stream()
            .filter(i -> i.getGastoPublico() != null && i.getGastoPublico().compareTo(BigDecimal.ZERO) > 0)
            .toList();

        List<BigDecimal> investimentos = indicadores.stream().map(Indicador::getGastoPublico).toList();
        Estatistica est = estatisticas(investimentos);

        List<Long> comBonus = new ArrayList<>();
        if (est.concentracaoElevada() && est.mediana() != null) {
            comBonus = indicadores.stream()
                .filter(i -> i.getGastoPublico().compareTo(est.mediana()) < 0)
                .map(i -> i.getMunicipio().getId())
                .toList();
        }

        return new EquidadeTerritorialDTO(anoRef, est.media(), est.desvio(), est.indice(),
            est.concentracaoElevada(), est.mediana(), est.concentracaoElevada() ? BONUS_SCORE : 0,
            comBonus, AlgoritmoMetadata.VERSAO);
    }

    /** Nucleo puro do calculo estatistico (testavel sem banco). */
    Estatistica estatisticas(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            return new Estatistica(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, false, null);
        }
        int n = valores.size();
        BigDecimal soma = valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal media = soma.divide(BigDecimal.valueOf(n), 6, RoundingMode.HALF_UP);

        BigDecimal somaQuadrados = BigDecimal.ZERO;
        for (BigDecimal v : valores) {
            BigDecimal diff = v.subtract(media);
            somaQuadrados = somaQuadrados.add(diff.multiply(diff));
        }
        BigDecimal variancia = somaQuadrados.divide(BigDecimal.valueOf(n), 6, RoundingMode.HALF_UP);
        BigDecimal desvio = variancia.sqrt(new java.math.MathContext(12)).setScale(2, RoundingMode.HALF_UP);

        BigDecimal indice = media.compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ONE
            : BigDecimal.ONE.subtract(desvio.divide(media, 6, RoundingMode.HALF_UP)).setScale(4, RoundingMode.HALF_UP);

        boolean concentracao = indice.compareTo(LIMIAR_CONCENTRACAO) < 0;
        return new Estatistica(media.setScale(2, RoundingMode.HALF_UP), desvio, indice, concentracao, mediana(valores));
    }

    private BigDecimal mediana(List<BigDecimal> valores) {
        List<BigDecimal> ordenados = new ArrayList<>(valores);
        ordenados.sort(BigDecimal::compareTo);
        int n = ordenados.size();
        if (n % 2 == 1) {
            return ordenados.get(n / 2);
        }
        return ordenados.get(n / 2 - 1).add(ordenados.get(n / 2))
            .divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
    }

    record Estatistica(BigDecimal media, BigDecimal desvio, BigDecimal indice,
                       boolean concentracaoElevada, BigDecimal mediana) {
    }
}
