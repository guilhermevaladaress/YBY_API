package com.yby.api.service.inteligencia;

import static org.assertj.core.api.Assertions.assertThat;

import com.yby.api.dto.SemaforoBiomaDTO;
import com.yby.api.entity.enums.Bioma;
import com.yby.api.entity.enums.VulnerabilidadeHidrica;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/** RN-106-A - Semaforo Bioma-Sensivel + RN-600 - Freshness. */
class SemaforoBiomaServiceTest {

    private final SemaforoBiomaService service = new SemaforoBiomaService(null, null);

    @Test
    void cerradoUsaLimiarMaisBaixoQueAmazonia() {
        // score 65: verde no Cerrado (>=60) porem amarelo na Amazonia (<75)
        assertThat(classificar(Bioma.CERRADO, "65").semaforo()).isEqualTo("verde");
        assertThat(classificar(Bioma.AMAZONIA, "65").semaforo()).isEqualTo("amarelo");
    }

    @Test
    void amazoniaVermelhoAbaixoDe50() {
        assertThat(classificar(Bioma.AMAZONIA, "49").semaforo()).isEqualTo("vermelho");
    }

    @Test
    void pantanalExigeBaixaVulnerabilidadeHidricaParaVerde() {
        SemaforoBiomaDTO comRisco = service.classificar(1L, Bioma.PANTANAL, new BigDecimal("80"),
            VulnerabilidadeHidrica.ALTA, false, false);
        assertThat(comRisco.semaforo()).isEqualTo("amarelo");

        SemaforoBiomaDTO seguro = service.classificar(1L, Bioma.PANTANAL, new BigDecimal("80"),
            VulnerabilidadeHidrica.BAIXA, false, false);
        assertThat(seguro.semaforo()).isEqualTo("verde");
    }

    @Test
    void prodesDesatualizadoRebaixaVerdeParaAmarelo() {
        SemaforoBiomaDTO r = service.classificar(1L, Bioma.CERRADO, new BigDecimal("90"),
            VulnerabilidadeHidrica.BAIXA, true, false);
        assertThat(r.semaforo()).isEqualTo("amarelo");
        assertThat(r.prodesDesatualizado()).isTrue();
    }

    private SemaforoBiomaDTO classificar(Bioma bioma, String score) {
        return service.classificar(1L, bioma, new BigDecimal(score),
            VulnerabilidadeHidrica.BAIXA, false, false);
    }
}
