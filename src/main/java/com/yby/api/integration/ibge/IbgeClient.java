package com.yby.api.integration.ibge;

import com.yby.api.config.IntegracaoProperties;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente da API publica e gratuita de Localidades do IBGE.
 *
 * <p>Fonte oficial para limites e codigos de municipios (AGENTS.md 5.4 - IBGE Geociencias).
 * Nao requer autenticacao. Mantem-se em {@code integration} conforme a arquitetura obrigatoria
 * (regra: integracoes nunca no controller).</p>
 */
@Component
public class IbgeClient {

    private static final ParameterizedTypeReference<List<IbgeMunicipioResponse>> LISTA_MUNICIPIOS =
        new ParameterizedTypeReference<>() {
        };

    private final RestClient restClient;
    private final IntegracaoProperties properties;

    public IbgeClient(RestClient integracaoRestClient, IntegracaoProperties properties) {
        this.restClient = integracaoRestClient;
        this.properties = properties;
    }

    /**
     * Lista os municipios de uma UF.
     *
     * @param ufSigla sigla da UF (ex.: {@code TO}); se nulo/vazio usa a UF default configurada
     * @return lista de municipios (id = codigo IBGE de 7 digitos, nome)
     */
    public List<IbgeMunicipioResponse> listarMunicipios(String ufSigla) {
        String uf = (ufSigla == null || ufSigla.isBlank()) ? properties.ufSigla() : ufSigla.trim().toUpperCase();
        List<IbgeMunicipioResponse> resposta = restClient.get()
            .uri(properties.ibge().baseUrl() + "/localidades/estados/{uf}/municipios", uf)
            .retrieve()
            .body(LISTA_MUNICIPIOS);
        return resposta == null ? List.of() : resposta;
    }
}
