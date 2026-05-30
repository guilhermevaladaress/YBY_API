package com.yby.api.dto;

import com.yby.api.entity.enums.InstituicaoTipo;
import com.yby.api.entity.enums.PadraoCertificacao;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Cadastro/saida de instituicao do mercado de credito de carbono.
 */
public record InstituicaoCarbonoDTO(
    Long id,
    @NotBlank String nome,
    @NotNull InstituicaoTipo tipo,
    PadraoCertificacao padraoCertificacao,
    String pais,
    @PositiveOrZero BigDecimal precoReferenciaTonelada,
    @Size(min = 3, max = 3) String moeda,
    String siteUrl,
    String apiUrl,
    @Email String contatoEmail,
    boolean compraJredd,
    Boolean ativo,
    String observacoes
) {
}
