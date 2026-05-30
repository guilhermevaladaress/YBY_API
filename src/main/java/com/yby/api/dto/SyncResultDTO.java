package com.yby.api.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Resultado de uma sincronizacao com fonte externa (ex.: IBGE).
 *
 * @param fonte         identificador da fonte (ex.: {@code IBGE})
 * @param totalRecebido registros retornados pela fonte
 * @param inseridos     novos registros criados
 * @param atualizados   registros existentes atualizados
 * @param ignorados     registros sem alteracao
 * @param avisos        mensagens nao fatais (ex.: codigo invalido)
 * @param executadoEm   timestamp ISO-8601 da execucao
 */
public record SyncResultDTO(
    String fonte,
    int totalRecebido,
    int inseridos,
    int atualizados,
    int ignorados,
    List<String> avisos,
    OffsetDateTime executadoEm
) {
}
