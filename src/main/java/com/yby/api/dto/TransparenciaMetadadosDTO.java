package com.yby.api.dto;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Metadados publicos de transparencia (RN-300).
 *
 * <p>Permite ao cidadao auditar a origem e a versao dos calculos sem autenticacao
 * (Lei 12.527/2011 - LAI; LC 131/2009 - Transparencia).</p>
 *
 * @param versaoAlgoritmo     versao do algoritmo de score/risco/alocacao
 * @param baseLegal           normas aplicadas aos calculos
 * @param totalMunicipios     municipios monitorados
 * @param ultimaAtualizacao   timestamp da atualizacao mais recente
 * @param pesosScore          pesos do score de prioridade (RN-101)
 */
public record TransparenciaMetadadosDTO(
    String versaoAlgoritmo,
    String baseLegal,
    long totalMunicipios,
    OffsetDateTime ultimaAtualizacao,
    Map<String, String> pesosScore
) {
}
