package com.yby.api.integration.inpe;

import com.yby.api.config.IntegracaoProperties;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente da API INPE Programa Queimadas.
 *
 * Focos de calor e incendios em tempo real e historico por municipio, bioma e estado.
 * Fonte: https://queimadas.dgi.inpe.br (Programa Queimadas INPE)
 *
 * Dados extremamente relevantes para areas em PRAD proximas a regioes de risco
 * e para o semaforo de prontidao do JREDD+.
 */
@Component
public class InpeQueimadaClient {

    private static final Logger log = LoggerFactory.getLogger(InpeQueimadaClient.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
        new ParameterizedTypeReference<>() {};

    private final RestClient restClient;
    private final IntegracaoProperties properties;

    public InpeQueimadaClient(RestClient integracaoRestClient, IntegracaoProperties properties) {
        this.restClient = integracaoRestClient;
        this.properties = properties;
    }

    /**
     * Retorna focos de queimada no Tocantins em um periodo.
     * Usa a API publica do INPE Queimadas.
     */
    public long contarFocosPorEstado(String estadoSigla, LocalDate inicio, LocalDate fim) {
        try {
            String url = properties.inpe().queimadaBaseUrl()
                + "/api/focos/count?estado=" + estadoSigla
                + "&dataInicio=" + inicio
                + "&dataFim=" + fim;

            Map<String, Object> response = restClient.get()
                .uri(url)
                .retrieve()
                .body(MAP_TYPE);

            if (response != null && response.containsKey("total")) {
                return ((Number) response.get("total")).longValue();
            }
            return 0L;
        } catch (Exception e) {
            log.warn("INPE Queimadas indisponivel ({} a {}): {}", inicio, fim, e.getMessage());
            return 0L;
        }
    }

    /**
     * Retorna o total de focos do ano corrente para o Tocantins.
     */
    public long focosAnoCorrente() {
        LocalDate inicio = LocalDate.now().withDayOfYear(1);
        return contarFocosPorEstado("TO", inicio, LocalDate.now());
    }

    /**
     * Retorna focos dos ultimos 30 dias para o Tocantins.
     */
    public long focosUltimos30Dias() {
        return contarFocosPorEstado("TO", LocalDate.now().minusDays(30), LocalDate.now());
    }

    /**
     * Lista focos de um municipio especifico.
     */
    public List<FocoQueimadaDTO> focosPorMunicipio(String municipio, LocalDate inicio, LocalDate fim) {
        try {
            String url = properties.inpe().queimadaBaseUrl()
                + "/api/focos?municipio=" + municipio + "&estado=TO"
                + "&dataInicio=" + inicio + "&dataFim=" + fim + "&limit=500";

            Map<String, Object> response = restClient.get()
                .uri(url)
                .retrieve()
                .body(MAP_TYPE);

            return parseFocos(response);
        } catch (Exception e) {
            log.warn("INPE Queimadas municipio {} indisponivel: {}", municipio, e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<FocoQueimadaDTO> parseFocos(Map<String, Object> response) {
        if (response == null) return Collections.emptyList();
        Object focos = response.get("focos");
        if (!(focos instanceof List<?> list)) return Collections.emptyList();
        return (List<FocoQueimadaDTO>) list;
    }
}
