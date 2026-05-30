package com.yby.api.service.inteligencia;

import static org.assertj.core.api.Assertions.assertThat;

import com.yby.api.service.inteligencia.EquidadeService.Estatistica;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

/** RN-500 - Equidade Territorial. */
class EquidadeServiceTest {

    private final EquidadeService service = new EquidadeService(null);

    @Test
    void distribuicaoUniformeTemEquidadeMaximaSemConcentracao() {
        Estatistica est = service.estatisticas(List.of(
            bd("100"), bd("100"), bd("100"), bd("100")));

        assertThat(est.desvio()).isEqualByComparingTo("0.00");
        assertThat(est.indice()).isEqualByComparingTo("1.0000");
        assertThat(est.concentracaoElevada()).isFalse();
    }

    @Test
    void distribuicaoConcentradaDisparaConcentracaoElevada() {
        Estatistica est = service.estatisticas(List.of(
            bd("10"), bd("10"), bd("10"), bd("1000")));

        assertThat(est.indice()).isLessThan(EquidadeService.LIMIAR_CONCENTRACAO);
        assertThat(est.concentracaoElevada()).isTrue();
        assertThat(est.mediana()).isEqualByComparingTo("10.00");
    }

    @Test
    void listaVaziaNaoQuebra() {
        Estatistica est = service.estatisticas(List.of());
        assertThat(est.concentracaoElevada()).isFalse();
        assertThat(est.mediana()).isNull();
    }

    private BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
