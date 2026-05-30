package com.yby.api.entity.enums;

/**
 * Nivel de vulnerabilidade hidrica do municipio.
 *
 * <p>Usado pela RN-106-A para calibrar o semaforo em biomas sensiveis a agua
 * (ex.: Pantanal exige baixa vulnerabilidade hidrica para receber verde).</p>
 */
public enum VulnerabilidadeHidrica {
    BAIXA,
    MEDIA,
    ALTA
}
