package com.yby.api.dto;

import java.time.OffsetDateTime;

/**
 * Resposta da solicitacao de redefinicao de senha.
 *
 * <p>A {@code mensagem} e sempre generica para nao revelar se o e-mail existe (evita
 * enumeracao de usuarios). Como o ambiente nao possui servico de e-mail configurado, o
 * {@code token} e devolvido aqui para permitir a redefinicao; em producao ele seria
 * enviado por e-mail e estes campos ficariam nulos.</p>
 */
public record EsqueciSenhaResponseDTO(
    String mensagem,
    String token,
    OffsetDateTime expiraEm
) {
}
