package com.yby.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/** Endpoints publicos de transparencia (RN-300): sem autenticacao. */
class PublicControllerIT extends AbstractApiIntegrationTest {

    @Test
    void ranking_publico_semAutenticacao_retorna200() throws Exception {
        mockMvc.perform(get("/api/v1/public/ranking").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void geojson_publico_semAutenticacao_retorna200() throws Exception {
        mockMvc.perform(get("/api/v1/public/geojson"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("FeatureCollection"));
    }
}
