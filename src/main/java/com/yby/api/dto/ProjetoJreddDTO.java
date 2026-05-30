package com.yby.api.dto;

import com.yby.api.entity.enums.ProjetoStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Projeto JREDD+ com meta de carbono, orcamento e marcos de execucao.
 *
 * @param percentualConclusaoMedio media de conclusao dos marcos (saida; calculado)
 */
public record ProjetoJreddDTO(
    Long id,
    @NotBlank String nome,
    String descricao,
    Long municipioId,
    ProjetoStatus status,
    LocalDate dataInicio,
    LocalDate dataFimPrevista,
    @PositiveOrZero BigDecimal metaTco2e,
    @PositiveOrZero BigDecimal orcamentoPrevisto,
    List<MarcoProjetoDTO> marcos,
    Integer percentualConclusaoMedio
) {
}
