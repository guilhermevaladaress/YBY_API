package com.yby.api.service.inteligencia;

import static org.assertj.core.api.Assertions.assertThat;

import com.yby.api.dto.ProjecaoDesmatamentoDTO;
import com.yby.api.dto.ValorAnualDTO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProjecaoDesmatamentoServiceTest {

    private final ProjecaoDesmatamentoService service = new ProjecaoDesmatamentoService(null, null);

    @Test
    void calcular_projetaComCagr() {
        List<ValorAnualDTO> historico = List.of(
            new ValorAnualDTO(2021, new BigDecimal("10")),
            new ValorAnualDTO(2022, new BigDecimal("20")),
            new ValorAnualDTO(2023, new BigDecimal("40")));

        ProjecaoDesmatamentoDTO dto = service.calcular(1L, historico, 2);

        // CAGR = (40/10)^(1/2) - 1 = 100% a.a.
        assertThat(dto.anoBase()).isEqualTo(2023);
        assertThat(dto.taxaMediaAnual()).isEqualByComparingTo("100.00");
        assertThat(dto.projecao()).hasSize(2);
        assertThat(dto.projecao().getFirst().ano()).isEqualTo(2024);
        assertThat(dto.projecao().getFirst().valor()).isEqualByComparingTo("80.00");
        assertThat(dto.projecao().getLast().valor()).isEqualByComparingTo("160.00");
    }

    @Test
    void calcular_historicoVazio_naoFalha() {
        ProjecaoDesmatamentoDTO dto = service.calcular(1L, List.of(), 3);
        assertThat(dto.projecao()).isEmpty();
        assertThat(dto.taxaMediaAnual()).isEqualByComparingTo("0");
    }
}
