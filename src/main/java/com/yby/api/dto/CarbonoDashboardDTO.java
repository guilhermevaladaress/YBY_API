package com.yby.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Painel executivo consolidado de carbono: projeção financeira, projetos JREDD+,
 * potencial de financiamento do Plano Safra e mercado comprador.
 *
 * <p>Inclui dados prontos para gráficos, cotação USD/BRL da data de referência,
 * totais em USD e BRL, e caixa informativa sobre crédito de carbono.</p>
 */
public record CarbonoDashboardDTO(
    OffsetDateTime geradoEm,
    long totalCreditos,
    long totalProjetosJredd,
    long totalInstituicoes,
    BigDecimal tco2eProjetadoTotal,
    BigDecimal receitaProjetadaTotalUsd,
    BigDecimal receitaProjetadaTotalReais,
    CotacaoDolarDTO cotacaoDolar,
    Map<String, Long> creditosPorStatus,
    List<ProjecaoAnualDTO> serieAnual,
    GraficoProjecaoDTO grafico,
    List<MunicipioReceitaDTO> topMunicipios,
    List<CompradorReferenciaDTO> melhoresCompradores,
    BigDecimal potencialFinanciamentoSafraReais,
    long linhasCarbonoAtivas,
    Map<String, Long> instituicoesPorTipo,
    InfoCreditoCarbonoDTO infoCreditoCarbono,
    String versaoAlgoritmo
) {

    /**
     * Receita de carbono projetada agregada por município.
     *
     * @param municipioId        município
     * @param tco2eTotal         toneladas projetadas no horizonte
     * @param receitaTotalUsd    receita projetada em USD
     * @param receitaTotalReais  receita projetada em R$
     */
    public record MunicipioReceitaDTO(
        Long municipioId,
        BigDecimal tco2eTotal,
        BigDecimal receitaTotalUsd,
        BigDecimal receitaTotalReais
    ) {
    }

    /**
     * Comprador/certificadora de referência no mercado.
     *
     * @param instituicaoId      id da instituição
     * @param nome               nome
     * @param tipo               papel no mercado
     * @param precoTonelada      preço de referência por tonelada na moeda original
     * @param precoToneladaBrl   preço convertido para R$ (quando moeda = USD)
     * @param moeda              moeda original do preço (USD ou BRL)
     */
    public record CompradorReferenciaDTO(
        Long instituicaoId,
        String nome,
        String tipo,
        BigDecimal precoTonelada,
        BigDecimal precoToneladaBrl,
        String moeda
    ) {
    }
}
