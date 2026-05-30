package com.yby.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yby.api.entity.enums.FonteDesmatamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

class DesmatamentoControllerIT extends AbstractApiIntegrationTest {

    private final int ano = Year.now().getValue();

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void historico_retornaLista() throws Exception {
        novoDesmatamento(municipio, LocalDate.now(), new BigDecimal("12.5"), FonteDesmatamento.PRODES);

        mockMvc.perform(get("/api/v1/desmatamento/" + municipio.getId() + "/historico"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void resumo_porAno_retorna200() throws Exception {
        novoDesmatamento(municipio, LocalDate.of(ano, 6, 1), new BigDecimal("30.0"), FonteDesmatamento.PRODES);

        mockMvc.perform(get("/api/v1/desmatamento/resumo").param("ano", String.valueOf(ano)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ano").value(ano));
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void importar_comGestor_retorna202ComJobId() throws Exception {
        mockMvc.perform(post("/api/v1/desmatamento/importar"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.jobId").isNotEmpty())
            .andExpect(jsonPath("$.status").value("PROCESSANDO"));
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void importar_comServidor_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/desmatamento/importar"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void statusImportacao_inexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/v1/desmatamento/importar/999999/status"))
            .andExpect(status().isNotFound());
    }
}
