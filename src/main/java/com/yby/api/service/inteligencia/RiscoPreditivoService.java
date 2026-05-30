package com.yby.api.service.inteligencia;

import com.yby.api.dto.RiscoPreditivoDTO;
import com.yby.api.entity.Alerta;
import com.yby.api.entity.Municipio;
import com.yby.api.entity.enums.AlertaTipo;
import com.yby.api.repository.AlertaRepository;
import com.yby.api.service.MunicipioService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * RN-108-A - Risco Preditivo com Historico.
 *
 * <p>Transforma o risco estatico em acao preventiva. A nota base (0-10) compoe embargos do
 * IBAMA, sobreposicoes com TI (FUNAI) e UC (ICMBio) e irregularidades de CAR (SICAR). Um fator
 * temporal ajusta a nota: embargo nos ultimos 6 meses agrava (x1,2); ausencia de embargo ha 2
 * anos alivia (x0,8); plano de acao ativo alivia (x0,9).</p>
 */
@Service
public class RiscoPreditivoService {

    private static final BigDecimal MAX = new BigDecimal("10");
    private static final BigDecimal FATOR_EMBARGO_RECENTE = new BigDecimal("1.2");
    private static final BigDecimal FATOR_SEM_EMBARGO = new BigDecimal("0.8");
    private static final BigDecimal FATOR_PLANO_ACAO = new BigDecimal("0.9");

    private final AlertaRepository alertaRepository;
    private final MunicipioService municipioService;

    public RiscoPreditivoService(AlertaRepository alertaRepository, MunicipioService municipioService) {
        this.alertaRepository = alertaRepository;
        this.municipioService = municipioService;
    }

    public RiscoPreditivoDTO avaliar(Long municipioId) {
        Municipio municipio = municipioService.buscarMunicipio(municipioId);
        List<Alerta> alertas = alertaRepository.findByMunicipioIdAndAtivoTrueOrderByCreatedAtDesc(municipioId);
        return calcular(municipioId, alertas, municipio.isPlanoAcaoAtivo(), LocalDate.now());
    }

    /** Nucleo puro do calculo (testavel sem banco). */
    RiscoPreditivoDTO calcular(Long municipioId, List<Alerta> alertas, boolean planoAcaoAtivo, LocalDate hoje) {
        BigDecimal notaBase = composicaoBase(alertas).min(MAX);

        boolean embargoRecente = alertas.stream()
            .filter(a -> a.getTipo() == AlertaTipo.EMBARGO)
            .anyMatch(a -> a.getDataAlerta() != null && !a.getDataAlerta().isBefore(hoje.minusMonths(6)));
        boolean semEmbargo2Anos = alertas.stream()
            .filter(a -> a.getTipo() == AlertaTipo.EMBARGO)
            .noneMatch(a -> a.getDataAlerta() != null && !a.getDataAlerta().isBefore(hoje.minusYears(2)));

        BigDecimal fatorTempo = BigDecimal.ONE;
        if (embargoRecente) {
            fatorTempo = fatorTempo.multiply(FATOR_EMBARGO_RECENTE);
        } else if (semEmbargo2Anos) {
            fatorTempo = fatorTempo.multiply(FATOR_SEM_EMBARGO);
        }
        if (planoAcaoAtivo) {
            fatorTempo = fatorTempo.multiply(FATOR_PLANO_ACAO);
        }

        BigDecimal notaFinal = notaBase.multiply(fatorTempo)
            .max(BigDecimal.ZERO).min(MAX).setScale(2, RoundingMode.HALF_UP);

        List<String> pendencias = alertas.stream()
            .map(a -> a.getTipo().name() + " (" + a.getGravidade().name() + "): "
                + a.getDescricao() + " | acao: " + a.getAcaoRecomendada())
            .toList();

        return new RiscoPreditivoDTO(municipioId, notaBase.setScale(2, RoundingMode.HALF_UP),
            fatorTempo.setScale(2, RoundingMode.HALF_UP), notaFinal, semaforo(notaFinal),
            embargoRecente, planoAcaoAtivo, pendencias, AlgoritmoMetadata.VERSAO);
    }

    private BigDecimal composicaoBase(List<Alerta> alertas) {
        BigDecimal nota = BigDecimal.ZERO;
        for (Alerta alerta : alertas) {
            BigDecimal pesoTipo = switch (alerta.getTipo()) {
                case EMBARGO -> new BigDecimal("3.0");        // Embargo IBAMA: maior peso
                case SOBREPOSICAO -> new BigDecimal("2.5");   // TI/FUNAI ou UC/ICMBio
                case CAR_IRREGULAR -> new BigDecimal("2.0");  // Irregularidade SICAR
                case MANUAL -> new BigDecimal("1.0");
            };
            BigDecimal pesoGravidade = switch (alerta.getGravidade()) {
                case ALTA -> BigDecimal.ONE;
                case MEDIA -> new BigDecimal("0.6");
                case BAIXA -> new BigDecimal("0.3");
            };
            nota = nota.add(pesoTipo.multiply(pesoGravidade));
        }
        return nota;
    }

    private String semaforo(BigDecimal nota) {
        if (nota.compareTo(new BigDecimal("6")) >= 0) {
            return "vermelho";
        }
        if (nota.compareTo(new BigDecimal("3")) >= 0) {
            return "amarelo";
        }
        return "verde";
    }
}
