package com.yby.api.service.inteligencia;

import static org.assertj.core.api.Assertions.assertThat;

import com.yby.api.dto.KpiMultidimensionalDTO;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** RN-103-B - KPI Multidimensional. */
class KpiMultidimensionalServiceTest {

    private final KpiMultidimensionalService service = new KpiMultidimensionalService(null);

    @Test
    void combinaDimensoesComPesosReponderados() {
        // ambiental=100, social=0, fiscal=0, pesos default 0.5/0.25/0.25, sem bonus
        KpiMultidimensionalDTO r = service.montar(1L, 2024,
            bd("100"), bd("0"), bd("0"), null, null, null, BigDecimal.ZERO);

        assertThat(r.kpiBase()).isEqualByComparingTo("50.00"); // 100*0.5
        assertThat(r.kpiFinal()).isEqualByComparingTo("50.00");
        assertThat(r.pesoAmbiental()).isEqualByComparingTo("0.50");
    }

    @Test
    void aplicaBonusSobreOKpiBase() {
        KpiMultidimensionalDTO r = service.montar(1L, 2024,
            bd("100"), bd("100"), bd("100"), null, null, null, bd("10"));

        assertThat(r.kpiBase()).isEqualByComparingTo("100.00");
        assertThat(r.kpiFinal()).isEqualByComparingTo("110.00"); // +10%
    }

    @Test
    void bonusSocialEscalonaEntre5e15PorCento() {
        // share = 10/100 = 0,10 -> metade do caminho ate 0,20 => 5 + 0,5*10 = 10
        assertThat(service.bonusSocial(bd("10"), bd("100"))).isEqualByComparingTo("10.00");
        // share >= 0,20 satura em 15
        assertThat(service.bonusSocial(bd("40"), bd("100"))).isEqualByComparingTo("15.00");
        // sem investimento social => 0
        assertThat(service.bonusSocial(BigDecimal.ZERO, bd("100"))).isEqualByComparingTo("0");
    }

    private BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
