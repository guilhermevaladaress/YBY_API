package com.yby.api.service.inteligencia;

import static org.assertj.core.api.Assertions.assertThat;

import com.yby.api.dto.CarbonoEvitadoDTO;
import com.yby.api.entity.enums.Bioma;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CarbonoEvitadoServiceTest {

    private final CarbonoEvitadoService service = new CarbonoEvitadoService(null, null);

    @Test
    void montar_calculaHectaresTco2eEValor() {
        CarbonoEvitadoDTO dto = service.montar(1L, 2026, Bioma.CERRADO,
            new BigDecimal("100"), new BigDecimal("40"), new BigDecimal("50.00"));

        // 100 - 40 = 60 ha evitados; 60 x 110 (Cerrado) = 6600 tCO2e; 6600 x 50 = 330000
        assertThat(dto.hectaresEvitados()).isEqualByComparingTo("60.00");
        assertThat(dto.tco2eEvitado()).isEqualByComparingTo("6600.0000");
        assertThat(dto.valorPotencialReais()).isEqualByComparingTo("330000.00");
        assertThat(dto.bioma()).isEqualTo(Bioma.CERRADO);
    }

    @Test
    void montar_semReducao_naoGeraCreditoNegativo() {
        CarbonoEvitadoDTO dto = service.montar(1L, 2026, Bioma.AMAZONIA,
            new BigDecimal("30"), new BigDecimal("80"), new BigDecimal("50.00"));

        assertThat(dto.hectaresEvitados()).isEqualByComparingTo("0.00");
        assertThat(dto.tco2eEvitado()).isEqualByComparingTo("0.0000");
        assertThat(dto.valorPotencialReais()).isEqualByComparingTo("0.00");
    }
}
