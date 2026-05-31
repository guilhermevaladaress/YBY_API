package com.yby.api.service.inteligencia;

import com.yby.api.integration.inpe.InpeQueimadaClient;
import org.springframework.stereotype.Service;

/**
 * Sinal recente de focos de calor (INPE Programa Queimadas) para a inteligencia preditiva.
 *
 * <p>A serie PRODES de desmatamento e anual e defasada; os focos de calor sao o sinal recente
 * (proxy de pressao ambiental no ano corrente). O {@code RecalculoInteligenciaService} usa o
 * total estadual aqui exposto e o distribui entre os municipios proporcionalmente ao desmatamento
 * historico recente, combinando assim a fonte INPE (atual) com a serie historica (peso).</p>
 *
 * <p>Best-effort: o {@link InpeQueimadaClient} ja trata indisponibilidade retornando 0,
 * de modo que a inteligencia nunca falha por ausencia da fonte externa (RN-102).</p>
 */
@Service
public class FocoCalorService {

    private final InpeQueimadaClient inpeQueimadaClient;

    public FocoCalorService(InpeQueimadaClient inpeQueimadaClient) {
        this.inpeQueimadaClient = inpeQueimadaClient;
    }

    /** Total de focos de calor no Tocantins no ano corrente (0 se a fonte estiver indisponivel). */
    public long focosEstadoAnoCorrente() {
        return inpeQueimadaClient.focosAnoCorrente();
    }
}
