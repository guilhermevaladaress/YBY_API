package com.yby.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Snapshot preditivo armazenado de um municipio (tabela projecoes_inteligencia).
 *
 * <p>Resultado persistido do recalculo: tendencia, projecao (serie JSON), risco e carbono evitado,
 * combinando a serie historica de desmatamento (PRODES) com os focos de calor do INPE.</p>
 */
public record ProjecaoArmazenadaDTO(
    Long municipioId,
    String nome,
    String codigoIbge,
    Integer anoBase,
    BigDecimal tendenciaPercent,
    String statusTendencia,
    Integer ajusteScore,
    BigDecimal taxaMediaAnual,
    BigDecimal mediaHistoricaHa,
    Integer horizonteAnos,
    String projecaoJson,
    BigDecimal notaRisco,
    String semaforoRisco,
    BigDecimal scoreProjetado,
    BigDecimal tco2eEvitado,
    BigDecimal valorPotencialReais,
    Integer focosCalorAno,
    String versaoAlgoritmo,
    OffsetDateTime calculadoEm
) {
}
