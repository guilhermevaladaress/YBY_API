package com.yby.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

class MunicipioControllerIT extends AbstractApiIntegrationTest {

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void listar_retornaPaginado() throws Exception {
        mockMvc.perform(get("/api/v1/municipios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void ranking_retornaOrdenadoPorScore() throws Exception {
        mockMvc.perform(get("/api/v1/municipios/ranking").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].codigoIbge").value("1721000"));
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void geojson_retornaFeatureCollection() throws Exception {
        mockMvc.perform(get("/api/v1/municipios/geojson"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type").value("FeatureCollection"));
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void buscarPorId_retornaDetalhe() throws Exception {
        mockMvc.perform(get("/api/v1/municipios/" + municipio.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codigoIbge").value("1721000"));
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void buscarInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/v1/municipios/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void criar_comGestor_retorna201() throws Exception {
        mockMvc.perform(post("/api/v1/municipios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Novo Municipio\",\"codigoIbge\":\"1700100\",\"scorePrioridade\":55.5,\"semaforo\":\"AMARELO\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigoIbge").value("1700100"));
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void criar_comServidor_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/municipios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"X\",\"codigoIbge\":\"1700100\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void atualizar_comGestor_retorna200() throws Exception {
        mockMvc.perform(put("/api/v1/municipios/" + municipio.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Palmas Atualizada\",\"scorePrioridade\":90.0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Palmas Atualizada"));
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void deletar_comGestor_retorna204() throws Exception {
        mockMvc.perform(delete("/api/v1/municipios/" + municipio.getId()))
            .andExpect(status().isNoContent());
    }
}
