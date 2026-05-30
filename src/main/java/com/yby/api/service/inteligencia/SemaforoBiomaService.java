package com.yby.api.service.inteligencia;

import com.yby.api.dto.SemaforoBiomaDTO;
import com.yby.api.entity.Municipio;
import com.yby.api.entity.enums.Bioma;
import com.yby.api.entity.enums.FonteDesmatamento;
import com.yby.api.entity.enums.VulnerabilidadeHidrica;
import com.yby.api.repository.DesmatamentoRepository;
import com.yby.api.service.MunicipioService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;

/**
 * RN-106-A - Semaforo Bioma-Sensivel + RN-600 - Freshness e Revalidacao.
 *
 * <p>Calibra o semaforo de prontidao por bioma (limiares distintos para Amazonia, Cerrado e
 * Pantanal) e aplica regras de obsolescencia de dados: PRODES com mais de 12 meses rebaixa
 * qualquer verde para amarelo; dados criticos sem atualizacao ha mais de 6 meses sinalizam
 * necessidade de revisao.</p>
 */
@Service
public class SemaforoBiomaService {

    private static final BigDecimal AMAZONIA_VERDE = new BigDecimal("75");
    private static final BigDecimal AMAZONIA_AMARELO = new BigDecimal("50");
    private static final BigDecimal CERRADO_VERDE = new BigDecimal("60");
    private static final BigDecimal CERRADO_AMARELO = new BigDecimal("40");
    private static final BigDecimal PANTANAL_VERDE = new BigDecimal("70");

    private final MunicipioService municipioService;
    private final DesmatamentoRepository desmatamentoRepository;

    public SemaforoBiomaService(MunicipioService municipioService,
                                DesmatamentoRepository desmatamentoRepository) {
        this.municipioService = municipioService;
        this.desmatamentoRepository = desmatamentoRepository;
    }

    public SemaforoBiomaDTO avaliar(Long municipioId) {
        Municipio municipio = municipioService.buscarMunicipio(municipioId);
        BigDecimal score = municipio.getScorePrioridade();
        Bioma bioma = municipio.getBioma() == null ? Bioma.CERRADO : municipio.getBioma();

        LocalDate ultimoProdes = desmatamentoRepository
            .maxDataReferenciaByMunicipioAndFonte(municipioId, FonteDesmatamento.PRODES);
        boolean prodesDesatualizado = ultimoProdes == null
            || ultimoProdes.isBefore(LocalDate.now().minusMonths(12));
        boolean dadosDesatualizados = municipio.getUltimaAtualizacao() == null
            || municipio.getUltimaAtualizacao().isBefore(OffsetDateTime.now().minusMonths(6));

        return classificar(municipioId, bioma, score, municipio.getVulnerabilidadeHidrica(),
            prodesDesatualizado, dadosDesatualizados);
    }

    /** Nucleo puro da classificacao (testavel sem banco). */
    SemaforoBiomaDTO classificar(Long municipioId, Bioma bioma, BigDecimal score,
                                 VulnerabilidadeHidrica vulnerabilidade,
                                 boolean prodesDesatualizado, boolean dadosDesatualizados) {
        BigDecimal safeScore = score == null ? BigDecimal.ZERO : score;
        StringBuilder justificativa = new StringBuilder();

        String semaforo = switch (bioma) {
            case AMAZONIA -> faixa(safeScore, AMAZONIA_VERDE, AMAZONIA_AMARELO, justificativa, "Amazonia");
            case PANTANAL -> semaforoPantanal(safeScore, vulnerabilidade, justificativa);
            case CERRADO -> faixa(safeScore, CERRADO_VERDE, CERRADO_AMARELO, justificativa, "Cerrado");
        };

        // RN-600: PRODES desatualizado nao pode sustentar um verde.
        if (prodesDesatualizado && "verde".equals(semaforo)) {
            semaforo = "amarelo";
            justificativa.append(" Rebaixado para amarelo: dados PRODES com mais de 12 meses (RN-600).");
        }
        if (dadosDesatualizados) {
            justificativa.append(" Atencao: dados criticos sem atualizacao ha mais de 6 meses exigem revisao (RN-600).");
        }

        return new SemaforoBiomaDTO(municipioId, bioma, safeScore, semaforo,
            justificativa.toString().trim(), dadosDesatualizados, prodesDesatualizado, AlgoritmoMetadata.VERSAO);
    }

    private String faixa(BigDecimal score, BigDecimal limiarVerde, BigDecimal limiarAmarelo,
                         StringBuilder justificativa, String nomeBioma) {
        if (score.compareTo(limiarVerde) >= 0) {
            justificativa.append(nomeBioma).append(": score ").append(score)
                .append(" >= ").append(limiarVerde).append(" -> verde.");
            return "verde";
        }
        if (score.compareTo(limiarAmarelo) >= 0) {
            justificativa.append(nomeBioma).append(": score ").append(score)
                .append(" na faixa [").append(limiarAmarelo).append(", ").append(limiarVerde).append(") -> amarelo.");
            return "amarelo";
        }
        justificativa.append(nomeBioma).append(": score ").append(score)
            .append(" < ").append(limiarAmarelo).append(" -> vermelho.");
        return "vermelho";
    }

    private String semaforoPantanal(BigDecimal score, VulnerabilidadeHidrica vulnerabilidade,
                                    StringBuilder justificativa) {
        boolean baixaVulnerabilidade = vulnerabilidade == null || vulnerabilidade == VulnerabilidadeHidrica.BAIXA;
        if (score.compareTo(PANTANAL_VERDE) >= 0 && baixaVulnerabilidade) {
            justificativa.append("Pantanal: score >= ").append(PANTANAL_VERDE)
                .append(" e baixa vulnerabilidade hidrica -> verde.");
            return "verde";
        }
        if (score.compareTo(PANTANAL_VERDE) >= 0) {
            justificativa.append("Pantanal: score >= ").append(PANTANAL_VERDE)
                .append(" porem vulnerabilidade hidrica ").append(vulnerabilidade)
                .append(" impede verde -> amarelo.");
            return "amarelo";
        }
        if (score.compareTo(CERRADO_AMARELO) >= 0) {
            justificativa.append("Pantanal: score intermediario -> amarelo.");
            return "amarelo";
        }
        justificativa.append("Pantanal: score baixo -> vermelho.");
        return "vermelho";
    }
}
