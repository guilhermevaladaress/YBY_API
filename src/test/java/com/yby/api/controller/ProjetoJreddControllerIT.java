package com.yby.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yby.api.entity.ProjetoJredd;
import com.yby.api.entity.enums.ProjetoStatus;
import com.yby.api.repository.ProjetoJreddRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

class ProjetoJreddControllerIT extends AbstractApiIntegrationTest {

    @Autowired
    private ProjetoJreddRepository projetoJreddRepository;

    private ProjetoJredd novoProjeto() {
        ProjetoJredd p = new ProjetoJredd();
        p.setNome("Projeto Restauracao Cerrado");
        p.setStatus(ProjetoStatus.PLANEJADO);
        p.setMunicipio(municipio);
        return projetoJreddRepository.save(p);
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void criar_comGestor_retorna201() throws Exception {
        mockMvc.perform(post("/api/v1/projetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Projeto A\",\"status\":\"PLANEJADO\",\"municipioId\":" + municipio.getId() + "}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome").value("Projeto A"));
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void criar_comServidor_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/projetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Projeto A\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void listar_retorna200() throws Exception {
        novoProjeto();
        mockMvc.perform(get("/api/v1/projetos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void adicionarMarco_comGestor_retorna201() throws Exception {
        ProjetoJredd projeto = novoProjeto();
        mockMvc.perform(post("/api/v1/projetos/" + projeto.getId() + "/marcos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"titulo\":\"Diagnostico\",\"status\":\"PENDENTE\",\"percentualConclusao\":0}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.titulo").value("Diagnostico"));
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void buscar_retorna200() throws Exception {
        ProjetoJredd projeto = novoProjeto();
        mockMvc.perform(get("/api/v1/projetos/" + projeto.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(projeto.getId()));
    }
}
