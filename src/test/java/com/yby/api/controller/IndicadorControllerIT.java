package com.yby.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Year;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

class IndicadorControllerIT extends AbstractApiIntegrationTest {

    private final int ano = Year.now().getValue();

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void kpi_retornaAgregadoDoPeriodo() throws Exception {
        novoIndicador(municipio, ano, new BigDecimal("1000.00"), new BigDecimal("500.00"));

        mockMvc.perform(get("/api/v1/indicadores/" + municipio.getId() + "/kpi")
                .param("anoInicio", String.valueOf(ano))
                .param("anoFim", String.valueOf(ano)))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void historico_retornaSerieAnual() throws Exception {
        novoIndicador(municipio, ano, new BigDecimal("1000.00"), new BigDecimal("500.00"));

        mockMvc.perform(get("/api/v1/indicadores/" + municipio.getId() + "/historico"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void upsert_comGestor_retorna201() throws Exception {
        mockMvc.perform(post("/api/v1/indicadores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"municipioId\":" + municipio.getId() + ",\"ano\":" + ano
                    + ",\"gastoPublico\":2000.00,\"resultadoAmbiental\":800.00}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ano").value(ano));
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void upsert_comAnoInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/v1/indicadores")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"municipioId\":" + municipio.getId() + ",\"ano\":1500}"))
            .andExpect(status().isBadRequest());
    }
}
