package com.yby.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Painel executivo consolidado de carbono: projecao financeira, projetos JREDD+,
 * potencial de financiamento do Plano Safra e mercado comprador.
 *
 * <p>Reune em um unico recurso os indicadores que alimentam o dashboard profissional do gestor.</p>
 */
public record CarbonoDashboardDTO(
    OffsetDateTime geradoEm,
    long totalCreditos,
    long totalProjetosJredd,
    long totalInstituicoes,
    BigDecimal tco2eProjetadoTotal,
    BigDecimal receitaProjetadaTotalReais,
    Map<String, Long> creditosPorStatus,
    List<ProjecaoAnualDTO> serieAnual,
    List<MunicipioReceitaDTO> topMunicipios,
    List<CompradorReferenciaDTO> melhoresCompradores,
    BigDecimal potencialFinanciamentoSafraReais,
    long linhasCarbonoAtivas,
    Map<String, Long> instituicoesPorTipo,
    String versaoAlgoritmo
) {

    /**
     * Receita de carbono projetada agregada por municipio.
     *
     * @param municipioId        municipio
     * @param tco2eTotal         toneladas projetadas no horizonte
     * @param receitaTotalReais  receita projetada (R$)
     */
    public record MunicipioReceitaDTO(
        Long municipioId,
        BigDecimal tco2eTotal,
        BigDecimal receitaTotalReais
    ) {
    }

    /**
     * Comprador/certificadora de referencia no mercado.
     *
     * @param instituicaoId  id da instituicao
     * @param nome           nome
     * @param tipo           papel no mercado
     * @param precoTonelada  preco de referencia por tonelada
     * @param moeda          moeda do preco
     */
    public record CompradorReferenciaDTO(
        Long instituicaoId,
        String nome,
        String tipo,
        BigDecimal precoTonelada,
        String moeda
    ) {
    }
}
