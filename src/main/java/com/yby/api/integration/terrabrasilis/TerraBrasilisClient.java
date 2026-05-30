package com.yby.api.integration.terrabrasilis;

import com.yby.api.config.IntegracaoProperties;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente da API TerraBrasilis (INPE) para dados de PRODES e DETER.
 *
 * PRODES: taxas anuais de desmatamento desde 1988.
 * DETER: alertas de desmatamento em tempo quase-real.
 *
 * Fonte: https://terrabrasilis.dpi.inpe.br (API publica, sem autenticacao)
 */
@Component
public class TerraBrasilisClient {

    private static final Logger log = LoggerFactory.getLogger(TerraBrasilisClient.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
        new ParameterizedTypeReference<>() {};

    private final RestClient restClient;
    private final IntegracaoProperties properties;

    public TerraBrasilisClient(RestClient integracaoRestClient, IntegracaoProperties properties) {
        this.restClient = integracaoRestClient;
        this.properties = properties;
    }

    /**
     * Retorna alertas DETER do Cerrado para o estado do Tocantins (TO).
     * Endpoint WFS público do TerraBrasilis.
     */
    public List<DeterAlertaDTO> alertasDeter(int ano) {
        try {
            String url = properties.terrabrasilis().deterBaseUrl()
                + "/ows?service=WFS&version=2.0.0&request=GetFeature"
                + "&typeName=deter-cerrado:deter_cerrado_state"
                + "&outputFormat=application/json"
                + "&CQL_FILTER=state_acr=%27TO%27+AND+EXTRACT(YEAR+FROM+view_date)=" + ano
                + "&count=1000";

            Map<String, Object> response = restClient.get()
                .uri(url)
                .retrieve()
                .body(MAP_TYPE);

            return parseFeatures(response, DeterAlertaDTO.class);
        } catch (Exception e) {
            log.warn("TerraBrasilis DETER indisponivel ({}): {}", ano, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Retorna dados PRODES anuais de desmatamento para o Tocantins.
     * Endpoint WFS público do TerraBrasilis.
     */
    public List<ProdesDTO> desmatamentoProdes(int ano) {
        try {
            String url = properties.terrabrasilis().prodesBaseUrl()
                + "/ows?service=WFS&version=2.0.0&request=GetFeature"
                + "&typeName=prodes-cerrado:yearly_deforestation_biome"
                + "&outputFormat=application/json"
                + "&CQL_FILTER=state_acr=%27TO%27+AND+year=" + ano;

            Map<String, Object> response = restClient.get()
                .uri(url)
                .retrieve()
                .body(MAP_TYPE);

            return parseFeatures(response, ProdesDTO.class);
        } catch (Exception e) {
            log.warn("TerraBrasilis PRODES indisponivel ({}): {}", ano, e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> parseFeatures(Map<String, Object> response, Class<T> type) {
        if (response == null) return Collections.emptyList();
        Object features = response.get("features");
        if (!(features instanceof List<?> list)) return Collections.emptyList();
        return (List<T>) list;
    }
}
