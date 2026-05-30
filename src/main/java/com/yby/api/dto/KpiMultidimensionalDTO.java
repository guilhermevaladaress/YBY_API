package com.yby.api.dto;

import java.math.BigDecimal;

/**
 * Saida da RN-103-B (KPI Multidimensional).
 *
 * <p>KPI = (ambiental x wa) + (social x ws) + (fiscal x wf), com bonus de 5-15% quando ha
 * investimento social relevante. As dimensoes sao normalizadas para 0-100 frente ao maior
 * valor estadual do ano, permitindo comparacao justa entre municipios.</p>
 */
public record KpiMultidimensionalDTO(
    Long municipioId,
    Integer ano,
    BigDecimal dimensaoAmbiental,
    BigDecimal dimensaoSocial,
    BigDecimal dimensaoFiscal,
    BigDecimal pesoAmbiental,
    BigDecimal pesoSocial,
    BigDecimal pesoFiscal,
    BigDecimal bonusPercentual,
    BigDecimal kpiBase,
    BigDecimal kpiFinal,
    String versaoAlgoritmo
) {
}
