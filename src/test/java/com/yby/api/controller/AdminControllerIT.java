package com.yby.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yby.api.dto.SyncResultDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

class AdminControllerIT extends AbstractApiIntegrationTest {

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void listarUsuarios_comGestor_retornaPaginado() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void listarUsuarios_comServidor_retorna403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void criarUsuario_comGestor_retorna201() throws Exception {
        mockMvc.perform(post("/api/v1/admin/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Novo\",\"email\":\"novo@yby.local\",\"role\":\"SERVIDOR\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("novo@yby.local"));
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void criarUsuario_emailDuplicado_retorna409() throws Exception {
        mockMvc.perform(post("/api/v1/admin/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Dup\",\"email\":\"" + SERVIDOR_EMAIL + "\",\"role\":\"SERVIDOR\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void atualizarStatus_comGestor_retorna200() throws Exception {
        Long servidorId = usuarioRepository.findByEmail(SERVIDOR_EMAIL).orElseThrow().getId();
        mockMvc.perform(patch("/api/v1/admin/usuarios/" + servidorId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ativo\":false}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void relatorio_comGestor_retorna200() throws Exception {
        mockMvc.perform(post("/api/v1/admin/relatorio"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void syncIbge_comGestor_retorna200() throws Exception {
        when(ibgeSyncService.sincronizarMunicipios(any()))
            .thenReturn(new SyncResultDTO("IBGE", 5, 5, 0, 0, java.util.List.of(), java.time.OffsetDateTime.now()));

        mockMvc.perform(post("/api/v1/admin/sync/ibge/municipios").param("uf", "TO"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void syncIbge_comServidor_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/sync/ibge/municipios"))
            .andExpect(status().isForbidden());
    }
}
