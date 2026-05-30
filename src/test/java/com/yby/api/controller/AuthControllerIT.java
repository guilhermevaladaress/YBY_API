package com.yby.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yby.api.entity.Usuario;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

class AuthControllerIT extends AbstractApiIntegrationTest {

    @Test
    void login_comCredenciaisValidas_retorna200ComToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + GESTOR_EMAIL + "\",\"senha\":\"" + SENHA_PADRAO + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.role").value("GESTOR"));
    }

    @Test
    void login_comSenhaErrada_retorna401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + GESTOR_EMAIL + "\",\"senha\":\"errada\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void esqueciSenha_emailExistente_retorna200ComToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/esqueci-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + GESTOR_EMAIL + "\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensagem").isNotEmpty())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void esqueciSenha_emailInexistente_retorna200SemToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/esqueci-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"naoexiste@yby.local\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void redefinirSenha_comTokenValido_retorna204ETrocaSenha() throws Exception {
        Usuario u = usuarioRepository.findByEmail(GESTOR_EMAIL).orElseThrow();
        u.setResetToken("token-valido-123");
        u.setResetTokenExpiraEm(OffsetDateTime.now().plusMinutes(10));
        usuarioRepository.save(u);

        mockMvc.perform(post("/api/v1/auth/redefinir-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"token-valido-123\",\"novaSenha\":\"novaSenha999\"}"))
            .andExpect(status().isNoContent());

        // login com a nova senha funciona
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + GESTOR_EMAIL + "\",\"senha\":\"novaSenha999\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void redefinirSenha_comTokenInvalido_retorna400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/redefinir-senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"inexistente\",\"novaSenha\":\"novaSenha999\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void me_autenticado_retornaUsuario() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(GESTOR_EMAIL));
    }

    @Test
    void me_semAutenticacao_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void changePassword_comSenhaAtualCorreta_retorna204() throws Exception {
        mockMvc.perform(patch("/api/v1/auth/senha")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"senhaAtual\":\"" + SENHA_PADRAO + "\",\"novaSenha\":\"outraSenha123\"}"))
            .andExpect(status().isNoContent());
    }
}
