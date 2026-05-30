package com.yby.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yby.api.entity.CreditoCarbono;
import com.yby.api.entity.enums.CreditoStatus;
import com.yby.api.repository.CreditoCarbonoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

class CreditoCarbonoControllerIT extends AbstractApiIntegrationTest {

    @Autowired
    private CreditoCarbonoRepository creditoCarbonoRepository;

    private CreditoCarbono novoCredito() {
        CreditoCarbono c = new CreditoCarbono();
        c.setMunicipio(municipio);
        c.setAnoBase(2026);
        c.setMetaTco2eAno(new BigDecimal("1000.0000"));
        c.setPrecoTonelada(new BigDecimal("50.0000"));
        c.setTaxaCrescimentoPreco(new BigDecimal("0.0500"));
        c.setPercentualCumprimentoMeta(new BigDecimal("1.0000"));
        c.setHorizonteAnos(5);
        c.setStatus(CreditoStatus.PLANEJADO);
        return creditoCarbonoRepository.save(c);
    }

    @Test
    @WithMockUser(username = GESTOR_EMAIL, roles = "GESTOR")
    void registrar_comGestor_retorna201() throws Exception {
        mockMvc.perform(post("/api/v1/creditos-carbono")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"municipioId\":" + municipio.getId()
                    + ",\"anoBase\":2026,\"metaTco2eAno\":1000.0,\"precoTonelada\":50.0,"
                    + "\"taxaCrescimentoPreco\":0.05,\"percentualCumprimentoMeta\":1.0,\"horizonteAnos\":5}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.municipioId").value(municipio.getId()));
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void registrar_comServidor_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/creditos-carbono")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"municipioId\":" + municipio.getId()
                    + ",\"anoBase\":2026,\"metaTco2eAno\":1000.0,\"precoTonelada\":50.0,\"horizonteAnos\":5}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void listar_retorna200() throws Exception {
        novoCredito();
        mockMvc.perform(get("/api/v1/creditos-carbono"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void projecao_retorna200ComItens() throws Exception {
        CreditoCarbono credito = novoCredito();
        mockMvc.perform(get("/api/v1/creditos-carbono/" + credito.getId() + "/projecao"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itens.length()").value(5))
            .andExpect(jsonPath("$.receitaTotalReais").isNotEmpty());
    }

    @Test
    @WithMockUser(roles = "SERVIDOR")
    void porMunicipio_retorna200() throws Exception {
        novoCredito();
        mockMvc.perform(get("/api/v1/creditos-carbono/municipio/" + municipio.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
