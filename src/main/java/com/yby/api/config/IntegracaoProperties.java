package com.yby.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuracao das integracoes externas (fontes oficiais de dados ambientais).
 *
 * <p>Os defaults apontam para as APIs publicas e gratuitas descritas no AGENTS.md
 * (secao 5.4). A UF default e Tocantins (codigo IBGE 17).</p>
 */
@ConfigurationProperties(prefix = "app.integracao")
public record IntegracaoProperties(
    @DefaultValue Ibge ibge,
    @DefaultValue("17") String ufCodigoIbge,
    @DefaultValue("TO") String ufSigla
) {

    public record Ibge(
        @DefaultValue("https://servicodados.ibge.gov.br/api/v1") String baseUrl,
        @DefaultValue("8000") int timeoutMs
    ) {
    }
}
