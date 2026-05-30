package com.yby.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yby.api.entity.enums.AlertaTipo;
import com.yby.api.entity.enums.Gravidade;
import java.time.Year;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

class AlertaControllerIT extends AbstractApiIntegrationTest {

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void desperdicio_retornaLista() throws Exception {
        mockMvc.perform(get("/api/v1/alertas/desperdicio").param("ano", String.valueOf(Year.now().getValue())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void risco_semBloqueio_retornaSemaforoVerde() throws Exception {
        mockMvc.perform(get("/api/v1/alertas/risco/" + municipio.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.semaforo").value("verde"));
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void risco_comEmbargoAtivo_forcaVermelho() throws Exception {
        // RN-106: embargo ativo e bloqueio critico -> vermelho independentemente da nota
        novoAlerta(municipio, AlertaTipo.EMBARGO, Gravidade.ALTA);

        mockMvc.perform(get("/api/v1/alertas/risco/" + municipio.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.semaforo").value("vermelho"));
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void upsert_comGestor_retorna201() throws Exception {
        mockMvc.perform(post("/api/v1/alertas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"municipioId\":" + municipio.getId()
                    + ",\"tipo\":\"MANUAL\",\"gravidade\":\"MEDIA\",\"descricao\":\"Teste\",\"acaoRecomendada\":\"Revisar\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tipo").value("MANUAL"));
    }
}
