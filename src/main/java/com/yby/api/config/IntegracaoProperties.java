package com.yby.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuracao das integracoes externas (fontes oficiais de dados ambientais do Tocantins).
 * Todas as APIs sao publicas e gratuitas, sem necessidade de autenticacao.
 */
@ConfigurationProperties(prefix = "app.integracao")
public record IntegracaoProperties(
    @DefaultValue Ibge ibge,
    @DefaultValue Terrabrasilis terrabrasilis,
    @DefaultValue Inpe inpe,
    @DefaultValue Seplan seplan,
    @DefaultValue("17") String ufCodigoIbge,
    @DefaultValue("TO") String ufSigla
) {

    public record Ibge(
        @DefaultValue("https://servicodados.ibge.gov.br/api/v1") String baseUrl,
        @DefaultValue("8000") int timeoutMs
    ) {}

    public record Terrabrasilis(
        @DefaultValue("https://terrabrasilis.dpi.inpe.br/geoserver/deter-cerrado") String deterBaseUrl,
        @DefaultValue("https://terrabrasilis.dpi.inpe.br/geoserver/prodes-cerrado") String prodesBaseUrl,
        @DefaultValue("10000") int timeoutMs
    ) {}

    public record Inpe(
        @DefaultValue("https://queimadas.dgi.inpe.br/queimadas/bdqueimadas-api") String queimadaBaseUrl,
        @DefaultValue("10000") int timeoutMs
    ) {}

    /**
     * Geoportal da SEPLAN-TO (Secretaria do Planejamento e Orcamento do Tocantins).
     * GeoServer publico com camadas territoriais usadas no estudo de areas de carbono.
     */
    public record Seplan(
        @DefaultValue("https://geoserver.seplan.to.gov.br/geoserver") String geoserverBaseUrl,
        @DefaultValue("10000") int timeoutMs
    ) {}
}
