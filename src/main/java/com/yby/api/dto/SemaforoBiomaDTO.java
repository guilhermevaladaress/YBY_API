package com.yby.api.dto;

import com.yby.api.entity.enums.Bioma;
import java.math.BigDecimal;

/**
 * Saida da RN-106-A (Semaforo Bioma-Sensivel) com a revalidacao de freshness da RN-600.
 *
 * @param municipioId            municipio avaliado
 * @param biomaPrincipal         bioma usado para calibrar os limiares
 * @param score                  score de prioridade considerado
 * @param semaforo               verde | amarelo | vermelho (apos eventuais rebaixamentos)
 * @param justificativaSemaforo  explicacao auditavel da classificacao
 * @param dadosDesatualizados    true quando ha dado critico sem atualizacao &gt; 6 meses (RN-600)
 * @param prodesDesatualizado    true quando PRODES &gt; 12 meses (forca amarelo - RN-600)
 * @param versaoAlgoritmo        versao do algoritmo (RN-300)
 */
public record SemaforoBiomaDTO(
    Long municipioId,
    Bioma biomaPrincipal,
    BigDecimal score,
    String semaforo,
    String justificativaSemaforo,
    boolean dadosDesatualizados,
    boolean prodesDesatualizado,
    String versaoAlgoritmo
) {
}
