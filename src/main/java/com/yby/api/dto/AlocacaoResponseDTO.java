package com.yby.api.dto;

import com.yby.api.entity.enums.EstrategiaAlocacao;
import java.math.BigDecimal;
import java.util.List;

/**
 * Saida da RN-200 (Otimizacao de Alocacao).
 *
 * @param orcamentoTotal         orcamento informado
 * @param ano                    ano de referencia
 * @param estrategia             estrategia aplicada
 * @param totalAlocado           soma efetivamente distribuida
 * @param retornoEsperadoTotal   soma dos retornos ambientais esperados
 * @param itens                  alocacoes por municipio (ordenadas por valor)
 * @param versaoAlgoritmo        versao do algoritmo (RN-300)
 */
public record AlocacaoResponseDTO(
    BigDecimal orcamentoTotal,
    Integer ano,
    EstrategiaAlocacao estrategia,
    BigDecimal totalAlocado,
    BigDecimal retornoEsperadoTotal,
    List<AlocacaoItemDTO> itens,
    String versaoAlgoritmo
) {
}
