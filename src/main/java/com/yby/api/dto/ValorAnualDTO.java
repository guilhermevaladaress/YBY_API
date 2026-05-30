package com.yby.api.dto;

import java.math.BigDecimal;

/**
 * Par (ano, valor) usado em series anuais (historico e projecao de desmatamento).
 */
public record ValorAnualDTO(
    Integer ano,
    BigDecimal valor
) {
}
