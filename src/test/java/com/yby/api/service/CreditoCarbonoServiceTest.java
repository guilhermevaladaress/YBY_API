package com.yby.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.yby.api.dto.ProjecaoAnualDTO;
import com.yby.api.entity.CreditoCarbono;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreditoCarbonoServiceTest {

    // O preço base é USD; usamos cotação fixa de R$5,70 para isolar o cálculo.
    private static final BigDecimal COTACAO = new BigDecimal("5.70");

    private final CreditoCarbonoService service =
        new CreditoCarbonoService(null, null, null, null);

    private CreditoCarbono credito() {
        CreditoCarbono c = new CreditoCarbono();
        c.setAnoBase(2026);
        c.setMetaTco2eAno(new BigDecimal("1000"));
        c.setPrecoTonelada(new BigDecimal("10")); // USD 10/tCO2e
        c.setTaxaCrescimentoPreco(new BigDecimal("0.05"));
        c.setPercentualCumprimentoMeta(BigDecimal.ONE);
        c.setHorizonteAnos(3);
        return c;
    }

    @Test
    void projetarItens_aplicaMetaECrescimentoDePreco() {
        List<ProjecaoAnualDTO> itens = service.projetarItens(credito(), COTACAO);

        assertThat(itens).hasSize(3);
        assertThat(itens.getFirst().ano()).isEqualTo(2026);
        assertThat(itens.getFirst().toneladasProjetadas()).isEqualByComparingTo("1000.0000");

        // Ano base (n=0): precoUSD=10, receitaUSD=10000, receitaBRL=57000
        assertThat(itens.getFirst().precoToneladaAnoUsd()).isEqualByComparingTo("10.0000");
        assertThat(itens.getFirst().receitaProjetadaUsd()).isEqualByComparingTo("10000.00");
        assertThat(itens.getFirst().receitaProjetadaReais()).isEqualByComparingTo("57000.00");

        // Ano +1 (n=1): precoUSD=10*1.05=10.5, receitaUSD=10500, receitaBRL=59850
        assertThat(itens.get(1).precoToneladaAnoUsd()).isEqualByComparingTo("10.5000");
        assertThat(itens.get(1).receitaProjetadaUsd()).isEqualByComparingTo("10500.00");
        assertThat(itens.get(1).receitaProjetadaReais()).isEqualByComparingTo("59850.00");

        // Ano +2 (n=2): precoUSD=10*1.1025=11.025, receitaUSD=11025, receitaBRL=62842.50
        assertThat(itens.get(2).precoToneladaAnoUsd()).isEqualByComparingTo("11.0250");
        assertThat(itens.get(2).receitaProjetadaUsd()).isEqualByComparingTo("11025.00");
        assertThat(itens.get(2).receitaProjetadaReais()).isEqualByComparingTo("62842.50");
    }

    @Test
    void projetarItens_semCumprimentoNemTaxa_usaPadroes() {
        CreditoCarbono c = credito();
        c.setPercentualCumprimentoMeta(null);
        c.setTaxaCrescimentoPreco(null);

        List<ProjecaoAnualDTO> itens = service.projetarItens(c, COTACAO);

        // Sem crescimento, receita USD constante = 1000 × 10 = 10000
        // Receita BRL = 10000 × 5.70 = 57000
        assertThat(itens.get(1).receitaProjetadaUsd()).isEqualByComparingTo("10000.00");
        assertThat(itens.get(1).receitaProjetadaReais()).isEqualByComparingTo("57000.00");
    }

    @Test
    void projetarItens_semCotacao_usaTaxaFallback() {
        List<ProjecaoAnualDTO> itens = service.projetarItens(credito(), null);

        // Fallback = R$5,70 — mesmo resultado dos outros testes
        assertThat(itens.getFirst().receitaProjetadaReais()).isEqualByComparingTo("57000.00");
    }
}
