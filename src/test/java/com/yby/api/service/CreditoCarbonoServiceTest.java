package com.yby.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.yby.api.dto.ProjecaoAnualDTO;
import com.yby.api.entity.CreditoCarbono;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class CreditoCarbonoServiceTest {

    private final CreditoCarbonoService service = new CreditoCarbonoService(null, null, null);

    private CreditoCarbono credito() {
        CreditoCarbono c = new CreditoCarbono();
        c.setAnoBase(2026);
        c.setMetaTco2eAno(new BigDecimal("1000"));
        c.setPrecoTonelada(new BigDecimal("50"));
        c.setTaxaCrescimentoPreco(new BigDecimal("0.05"));
        c.setPercentualCumprimentoMeta(BigDecimal.ONE);
        c.setHorizonteAnos(3);
        return c;
    }

    @Test
    void projetarItens_aplicaMetaECrescimentoDePreco() {
        List<ProjecaoAnualDTO> itens = service.projetarItens(credito());

        assertThat(itens).hasSize(3);
        assertThat(itens.getFirst().ano()).isEqualTo(2026);
        assertThat(itens.getFirst().toneladasProjetadas()).isEqualByComparingTo("1000.0000");
        // ano base: 1000 x 50 = 50000
        assertThat(itens.getFirst().receitaProjetadaReais()).isEqualByComparingTo("50000.00");
        // ano +1: preco 52.5 -> 52500 ; ano +2: preco 55.125 -> 55125
        assertThat(itens.get(1).receitaProjetadaReais()).isEqualByComparingTo("52500.00");
        assertThat(itens.get(2).receitaProjetadaReais()).isEqualByComparingTo("55125.00");
    }

    @Test
    void projetarItens_semCumprimentoNemTaxa_usaPadroes() {
        CreditoCarbono c = credito();
        c.setPercentualCumprimentoMeta(null);
        c.setTaxaCrescimentoPreco(null);

        List<ProjecaoAnualDTO> itens = service.projetarItens(c);

        // cumprimento padrao 100% e taxa 0%: receita constante de 50000
        assertThat(itens.get(1).receitaProjetadaReais()).isEqualByComparingTo("50000.00");
    }
}
