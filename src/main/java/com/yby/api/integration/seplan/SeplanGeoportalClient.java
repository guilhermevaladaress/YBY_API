package com.yby.api.integration.seplan;

import com.yby.api.config.IntegracaoProperties;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

/**
 * Cliente do Geoportal da SEPLAN-TO (Secretaria do Planejamento e Orcamento do Tocantins).
 *
 * <p>Consome o GeoServer publico via protocolo WFS (Web Feature Service) para obter camadas
 * territoriais usadas no estudo de areas de carbono: cobertura vegetal, unidades de conservacao,
 * zoneamento ecologico-economico, assentamentos e bacias hidrograficas.</p>
 *
 * <p>Resiliente (RN-600): qualquer indisponibilidade da fonte externa retorna vazio em vez de
 * propagar erro, preservando os endpoints que dependem apenas de dados locais.</p>
 */
@Component
public class SeplanGeoportalClient {

    private static final Logger log = LoggerFactory.getLogger(SeplanGeoportalClient.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
        new ParameterizedTypeReference<>() {};

    private final RestClient restClient;
    private final IntegracaoProperties properties;

    public SeplanGeoportalClient(RestClient integracaoRestClient, IntegracaoProperties properties) {
        this.restClient = integracaoRestClient;
        this.properties = properties;
    }

    /**
     * Busca feicoes de uma camada do GeoServer da SEPLAN via WFS 2.0 (GetFeature, GeoJSON).
     *
     * @param typeName   nome da camada (ex.: {@code seplan:cobertura_vegetal})
     * @param cqlFilter  filtro CQL opcional (ex.: {@code municipio='PALMAS'}); pode ser nulo
     * @param maxFeatures limite de feicoes retornadas
     * @return mapa com {@code total} e {@code features}; vazio quando a fonte esta indisponivel
     */
    @SuppressWarnings("unchecked")
    public SeplanFeatureCollectionDTO buscarCamada(String typeName, String cqlFilter, int maxFeatures) {
        try {
            StringBuilder url = new StringBuilder(properties.seplan().geoserverBaseUrl())
                .append("/ows?service=WFS&version=2.0.0&request=GetFeature")
                .append("&typeName=").append(UriUtils.encodeQueryParam(typeName, "UTF-8"))
                .append("&outputFormat=application/json")
                .append("&count=").append(maxFeatures);
            if (cqlFilter != null && !cqlFilter.isBlank()) {
                url.append("&CQL_FILTER=").append(UriUtils.encodeQueryParam(cqlFilter, "UTF-8"));
            }

            Map<String, Object> response = restClient.get()
                .uri(url.toString())
                .retrieve()
                .body(MAP_TYPE);

            if (response == null) {
                return SeplanFeatureCollectionDTO.vazia(typeName);
            }
            Object features = response.get("features");
            List<Map<String, Object>> lista = features instanceof List<?> l
                ? (List<Map<String, Object>>) l
                : Collections.emptyList();
            Object total = response.getOrDefault("totalFeatures", lista.size());
            long totalFeatures = total instanceof Number n ? n.longValue() : lista.size();
            return new SeplanFeatureCollectionDTO(typeName, totalFeatures, lista, true);
        } catch (Exception e) {
            log.warn("Geoportal SEPLAN indisponivel para camada {}: {}", typeName, e.getMessage());
            return SeplanFeatureCollectionDTO.vazia(typeName);
        }
    }
}
