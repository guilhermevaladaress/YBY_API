package com.yby.api.dto;

import com.yby.api.entity.enums.MarcoStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Marco (etapa) de um projeto JREDD+.
 */
public record MarcoProjetoDTO(
    Long id,
    Long projetoId,
    @NotBlank String titulo,
    String descricao,
    LocalDate dataPrevista,
    LocalDate dataConclusao,
    MarcoStatus status,
    @Min(0) @Max(100) Integer percentualConclusao
) {
}
