package com.yby.api.integration.seplan;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Colecao de feicoes (features GeoJSON) retornada pelo Geoportal da SEPLAN-TO.
 *
 * @param typeName       camada consultada
 * @param totalFeatures  total de feicoes na fonte
 * @param features       feicoes retornadas (atributos + geometria)
 * @param disponivel     indica se a fonte respondeu com sucesso
 */
public record SeplanFeatureCollectionDTO(
    String typeName,
    long totalFeatures,
    List<Map<String, Object>> features,
    boolean disponivel
) {

    static SeplanFeatureCollectionDTO vazia(String typeName) {
        return new SeplanFeatureCollectionDTO(typeName, 0, Collections.emptyList(), false);
    }
}
