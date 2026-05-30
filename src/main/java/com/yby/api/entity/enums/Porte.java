package com.yby.api.entity.enums;

import java.math.BigDecimal;

/**
 * Porte territorial do municipio, usado para benchmarking entre semelhantes (RN-107-A).
 * Faixas em hectares baseadas na distribuicao de areas municipais.
 */
public enum Porte {
    PEQUENO,
    MEDIO,
    GRANDE,
    INDEFINIDO;

    private static final BigDecimal LIMITE_PEQUENO = new BigDecimal("200000");
    private static final BigDecimal LIMITE_MEDIO = new BigDecimal("1000000");

    public static Porte deArea(BigDecimal areaHa) {
        if (areaHa == null || areaHa.compareTo(BigDecimal.ZERO) <= 0) {
            return INDEFINIDO;
        }
        if (areaHa.compareTo(LIMITE_PEQUENO) < 0) {
            return PEQUENO;
        }
        if (areaHa.compareTo(LIMITE_MEDIO) < 0) {
            return MEDIO;
        }
        return GRANDE;
    }
}
