package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado do matching de um credito de carbono com instituicoes compradoras.
 *
 * <p>Para a quantidade total de tCO2e projetada do credito, lista os melhores compradores
 * por receita estimada (preco de referencia x toneladas), do maior para o menor.</p>
 *
 * @param creditoId         credito avaliado
 * @param municipioId       municipio do credito
 * @param tco2eTotal        toneladas projetadas no horizonte do credito
 * @param compradores       instituicoes ordenadas por receita estimada (desc)
 * @param versaoAlgoritmo   versao do algoritmo (RN-300)
 */
public record MatchCompradorDTO(
    Long creditoId,
    Long municipioId,
    BigDecimal tco2eTotal,
    List<MatchItemDTO> compradores,
    String versaoAlgoritmo
) {

    /**
     * Um comprador potencial e a receita estimada.
     *
     * @param instituicaoId        id da instituicao
     * @param nome                 nome da instituicao
     * @param tipo                 papel no mercado
     * @param padraoCertificacao   padrao de certificacao (pode ser nulo)
     * @param precoTonelada        preco de referencia por tonelada
     * @param moeda                moeda do preco
     * @param receitaEstimada      tco2eTotal x precoTonelada (na moeda da instituicao)
     */
    public record MatchItemDTO(
        Long instituicaoId,
        String nome,
        String tipo,
        String padraoCertificacao,
        BigDecimal precoTonelada,
        String moeda,
        BigDecimal receitaEstimada
    ) {
    }
}
