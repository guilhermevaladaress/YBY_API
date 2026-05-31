package com.yby.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cotação USD/BRL em uma data de referência.
 *
 * @param dataReferencia  data usada para buscar a cotação
 * @param taxaUsdBrl      quantos reais equivalem a 1 dólar (bid)
 * @param fonte           origem da cotação ("AwesomeAPI", "fallback")
 */
public record CotacaoDolarDTO(
    LocalDate dataReferencia,
    BigDecimal taxaUsdBrl,
    String fonte
) {
}
