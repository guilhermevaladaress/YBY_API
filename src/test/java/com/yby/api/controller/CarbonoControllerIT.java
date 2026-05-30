package com.yby.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

class CarbonoControllerIT extends AbstractApiIntegrationTest {

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void historicoMenores_retornaLista() throws Exception {
        novaEmissao(municipio, LocalDate.of(2024, 12, 31), new BigDecimal("1234.56"));

        mockMvc.perform(get("/api/v1/carbono/historico/menores"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void relatorioPdf_retornaArquivoPdf() throws Exception {
        novaEmissao(municipio, LocalDate.of(2024, 12, 31), new BigDecimal("1234.56"));

        mockMvc.perform(get("/api/v1/carbono/historico/menores/relatorio/pdf"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE));
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void registrar_comGestor_retorna201() throws Exception {
        mockMvc.perform(post("/api/v1/carbono/registros")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"municipioId\":" + municipio.getId()
                    + ",\"dataReferencia\":\"2024-12-31\",\"emissaoTco2e\":999.99,\"fonte\":\"SEEG\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.fonte").value("SEEG"));
    }
}
