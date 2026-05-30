package com.yby.api.service.inteligencia;

import static org.assertj.core.api.Assertions.assertThat;

import com.yby.api.dto.TendenciaDesmatamentoDTO;
import com.yby.api.entity.enums.StatusTendencia;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** RN-101-A - Score de Prioridade com Tendencia. */
class TendenciaServiceTest {

    private final TendenciaService service = new TendenciaService(null, null);

    @Test
    void acelerandoQuandoVariacaoAcimaDe10Porcento() {
        TendenciaDesmatamentoDTO r = service.avaliar(1L, 2024, bd("120"), bd("100"));

        assertThat(r.tendenciaDesmatamento()).isEqualByComparingTo("20.00");
        assertThat(r.statusTendencia()).isEqualTo(StatusTendencia.ACELERANDO);
        assertThat(r.ajusteScore()).isEqualTo(15);
    }

    @Test
    void melhorandoQuandoVariacaoAbaixoDeMenos10Porcento() {
        TendenciaDesmatamentoDTO r = service.avaliar(1L, 2024, bd("80"), bd("100"));

        assertThat(r.tendenciaDesmatamento()).isEqualByComparingTo("-20.00");
        assertThat(r.statusTendencia()).isEqualTo(StatusTendencia.MELHORANDO);
        assertThat(r.ajusteScore()).isEqualTo(-10);
    }

    @Test
    void estavelDentroDaFaixaDeDezPorcento() {
        TendenciaDesmatamentoDTO r = service.avaliar(1L, 2024, bd("105"), bd("100"));

        assertThat(r.statusTendencia()).isEqualTo(StatusTendencia.ESTAVEL);
        assertThat(r.ajusteScore()).isZero();
    }

    @Test
    void anoAnteriorZeroNaoDivideNemQuebra() {
        TendenciaDesmatamentoDTO surgiu = service.avaliar(1L, 2024, bd("50"), BigDecimal.ZERO);
        assertThat(surgiu.statusTendencia()).isEqualTo(StatusTendencia.ACELERANDO);

        TendenciaDesmatamentoDTO semDesmatamento = service.avaliar(1L, 2024, BigDecimal.ZERO, BigDecimal.ZERO);
        assertThat(semDesmatamento.statusTendencia()).isEqualTo(StatusTendencia.ESTAVEL);
    }

    private BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
