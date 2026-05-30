package com.yby.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({AppSecurityProperties.class, IntegracaoProperties.class})
public class AppConfig {

    /**
     * RestClient compartilhado pelas integracoes externas (IBGE, TerraBrasilis, etc.).
     * Timeouts curtos evitam que uma fonte instavel trave a aplicacao (RN-600 / resiliencia).
     */
    @Bean
    public RestClient integracaoRestClient(IntegracaoProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.ibge().timeoutMs());
        factory.setReadTimeout(properties.ibge().timeoutMs());
        return RestClient.builder()
            .requestFactory(factory)
            .build();
    }
}

