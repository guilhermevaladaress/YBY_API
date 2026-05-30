package com.yby.api.entity.enums;

/**
 * Estrategia de otimizacao da alocacao orcamentaria (RN-200).
 */
public enum EstrategiaAlocacao {
    /** Maximiza o retorno ambiental por real (prioriza maior KPI/score). */
    MAXIMO_KPI,
    /** Distribui de forma desconcentrada entre os municipios elegiveis (RN-500). */
    EQUIDADE_REGIONAL,
    /** Prioriza municipios com melhor relacao prioridade/risco (menor exposicao). */
    RISCO_MINIMO
}
