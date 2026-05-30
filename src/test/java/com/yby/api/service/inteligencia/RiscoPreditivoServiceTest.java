package com.yby.api.service.inteligencia;

import static org.assertj.core.api.Assertions.assertThat;

import com.yby.api.dto.RiscoPreditivoDTO;
import com.yby.api.entity.Alerta;
import com.yby.api.entity.enums.AlertaTipo;
import com.yby.api.entity.enums.Gravidade;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** RN-108-A - Risco Preditivo com Historico. */
class RiscoPreditivoServiceTest {

    private final RiscoPreditivoService service = new RiscoPreditivoService(null, null);
    private final LocalDate hoje = LocalDate.of(2026, 5, 30);

    @Test
    void embargoRecenteAgravaEmVinteporcento() {
        Alerta embargo = alerta(AlertaTipo.EMBARGO, Gravidade.ALTA, hoje.minusMonths(2));
        RiscoPreditivoDTO r = service.calcular(1L, List.of(embargo), false, hoje);

        assertThat(r.notaBase()).isEqualByComparingTo("3.00");   // 3.0 x 1.0
        assertThat(r.fatorTempo()).isEqualByComparingTo("1.20");
        assertThat(r.notaRiscoFinal()).isEqualByComparingTo("3.60");
        assertThat(r.embargoRecente()).isTrue();
    }

    @Test
    void ausenciaDeEmbargoAliviaEmVinteporcento() {
        Alerta car = alerta(AlertaTipo.CAR_IRREGULAR, Gravidade.ALTA, hoje.minusMonths(1));
        RiscoPreditivoDTO r = service.calcular(1L, List.of(car), false, hoje);

        assertThat(r.notaBase()).isEqualByComparingTo("2.00");
        assertThat(r.fatorTempo()).isEqualByComparingTo("0.80");
        assertThat(r.notaRiscoFinal()).isEqualByComparingTo("1.60");
        assertThat(r.embargoRecente()).isFalse();
    }

    @Test
    void planoDeAcaoAtivoReduzNotaAdicionalmente() {
        Alerta car = alerta(AlertaTipo.CAR_IRREGULAR, Gravidade.ALTA, hoje.minusMonths(1));
        RiscoPreditivoDTO r = service.calcular(1L, List.of(car), true, hoje);

        // 2.0 (base) x 0.8 (sem embargo) x 0.9 (plano) = 1.44
        assertThat(r.fatorTempo()).isEqualByComparingTo("0.72");
        assertThat(r.notaRiscoFinal()).isEqualByComparingTo("1.44");
    }

    @Test
    void notaNuncaUltrapassaDez() {
        Alerta e1 = alerta(AlertaTipo.EMBARGO, Gravidade.ALTA, hoje.minusMonths(1));
        Alerta e2 = alerta(AlertaTipo.EMBARGO, Gravidade.ALTA, hoje.minusMonths(1));
        Alerta e3 = alerta(AlertaTipo.SOBREPOSICAO, Gravidade.ALTA, hoje.minusMonths(1));
        RiscoPreditivoDTO r = service.calcular(1L, List.of(e1, e2, e3), false, hoje);

        assertThat(r.notaRiscoFinal()).isEqualByComparingTo("10.00");
    }

    private Alerta alerta(AlertaTipo tipo, Gravidade gravidade, LocalDate data) {
        Alerta a = new Alerta();
        a.setTipo(tipo);
        a.setGravidade(gravidade);
        a.setDataAlerta(data);
        a.setDescricao("teste");
        a.setAcaoRecomendada("acao");
        return a;
    }
}
