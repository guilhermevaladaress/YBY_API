package com.yby.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yby.api.entity.enums.FonteDesmatamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

class InteligenciaControllerIT extends AbstractApiIntegrationTest {

    private final int ano = Year.now().getValue();

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void tendencia_retorna200() throws Exception {
        novoDesmatamento(municipio, LocalDate.of(ano, 6, 1), new BigDecimal("50.0"), FonteDesmatamento.PRODES);
        novoDesmatamento(municipio, LocalDate.of(ano - 1, 6, 1), new BigDecimal("40.0"), FonteDesmatamento.PRODES);

        mockMvc.perform(get("/api/v1/inteligencia/tendencia/" + municipio.getId()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void kpiMultidimensional_retorna200() throws Exception {
        novoIndicador(municipio, ano, new BigDecimal("1000.00"), new BigDecimal("500.00"));

        mockMvc.perform(get("/api/v1/inteligencia/kpi-multidimensional/" + municipio.getId())
                .param("ano", String.valueOf(ano)))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void semaforoBioma_retorna200() throws Exception {
        mockMvc.perform(get("/api/v1/inteligencia/semaforo-bioma/" + municipio.getId()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void riscoPreditivo_retorna200() throws Exception {
        mockMvc.perform(get("/api/v1/inteligencia/risco-preditivo/" + municipio.getId()))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void desperdicio_retorna200() throws Exception {
        mockMvc.perform(get("/api/v1/inteligencia/desperdicio"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void equidade_retorna200() throws Exception {
        mockMvc.perform(get("/api/v1/inteligencia/equidade"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void alocacao_comGestor_retorna200() throws Exception {
        novoIndicador(municipio, ano, new BigDecimal("1000.00"), new BigDecimal("500.00"));

        mockMvc.perform(post("/api/v1/inteligencia/alocacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orcamentoTotal\":1000000.00,\"ano\":" + ano + ",\"estrategia\":\"MAXIMO_KPI\"}"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void alocacao_comServidor_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/inteligencia/alocacao")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"orcamentoTotal\":1000000.00,\"estrategia\":\"MAXIMO_KPI\"}"))
            .andExpect(status().isForbidden());
    }
}
