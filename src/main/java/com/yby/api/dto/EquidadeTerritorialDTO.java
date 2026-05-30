package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Saida da RN-500 (Equidade Territorial).
 *
 * <p>Mede a concentracao do investimento publico entre municipios. O indice de equidade
 * (1 - desvio/media) varia de forma que valores baixos indicam alta concentracao. Abaixo de
 * 0,3 recomenda-se bonus de score aos municipios abaixo da mediana, para desconcentrar o
 * investimento (isonomia - CF art. 5; eficiencia - CF art. 37).</p>
 *
 * @param ano                  ano avaliado
 * @param media                investimento medio
 * @param desvioPadrao         desvio padrao do investimento
 * @param indiceEquidade       1 - (desvio/media)
 * @param concentracaoElevada  true quando indice &lt; 0,3 (aciona bonus)
 * @param mediana              mediana do investimento
 * @param bonusScore           pontos de bonus sugeridos aos municipios abaixo da mediana
 * @param municipiosComBonus   ids dos municipios elegiveis ao bonus
 * @param versaoAlgoritmo      versao do algoritmo (RN-300)
 */
public record EquidadeTerritorialDTO(
    Integer ano,
    BigDecimal media,
    BigDecimal desvioPadrao,
    BigDecimal indiceEquidade,
    boolean concentracaoElevada,
    BigDecimal mediana,
    int bonusScore,
    List<Long> municipiosComBonus,
    String versaoAlgoritmo
) {
}
