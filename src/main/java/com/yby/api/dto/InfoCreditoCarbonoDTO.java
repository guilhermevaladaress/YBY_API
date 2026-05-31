package com.yby.api.dto;

/**
 * Conteúdo informativo sobre crédito de carbono exibido no painel como caixa educacional.
 *
 * @param oQueE                  explicação sobre o que é crédito de carbono
 * @param origemRecurso          de onde vêm os recursos financeiros
 * @param comoFuncionaProjecao   como a projeção financeira é calculada
 * @param unidadeMedida          explicação sobre tCO2e
 * @param padroesCertificacao    padrões internacionais reconhecidos
 */
public record InfoCreditoCarbonoDTO(
    String oQueE,
    String origemRecurso,
    String comoFuncionaProjecao,
    String unidadeMedida,
    String padroesCertificacao
) {

    /** Conteúdo padrão exibido no painel de carbono. */
    public static InfoCreditoCarbonoDTO padrao() {
        return new InfoCreditoCarbonoDTO(
            "O crédito de carbono é um certificado que representa a redução ou remoção de " +
            "1 tonelada de CO₂ equivalente (tCO₂e) da atmosfera. No contexto JREDD+ " +
            "(Jurisdictional REDD+), cada hectare de floresta mantido em pé — em relação à " +
            "linha de base histórica de desmatamento — gera créditos que o estado pode " +
            "comercializar no mercado voluntário ou sob acordos bilaterais.",

            "Os recursos são provenientes do mercado voluntário de carbono: empresas e " +
            "governos que desejam compensar suas emissões pagam pelo crédito em dólares " +
            "(USD), o padrão global do setor. Iniciativas como a Coalizão LEAF e a " +
            "plataforma ART/TREES garantem um preço mínimo de USD 10/tCO₂e para créditos " +
            "jurisdicionais certificados. O Estado do Tocantins recebe o valor em reais, " +
            "convertido pela cotação do dólar na data de referência do contrato.",

            "A projeção financeira é calculada ano a ano com base em três parâmetros: " +
            "(1) Meta anual de tCO₂e — quantidade de toneladas planejadas para o período; " +
            "(2) Percentual de cumprimento da meta — expectativa de atingimento (ex.: 85%); " +
            "(3) Preço por tonelada em USD, com crescimento anual composto (taxa a.a.). " +
            "Para cada ano N, o preço projetado é: PreçoBase × (1 + Taxa)ᴺ. " +
            "A receita em reais é obtida multiplicando a receita em USD pela cotação " +
            "USD/BRL da data de referência escolhida pelo usuário.",

            "tCO₂e significa 'tonelada de CO₂ equivalente'. É a unidade padrão que " +
            "normaliza diferentes gases de efeito estufa (CO₂, CH₄, N₂O etc.) em uma " +
            "única métrica comparável, usando o Potencial de Aquecimento Global (GWP) " +
            "de cada gás. 1 ha de floresta amazônica preservada equivale a " +
            "aproximadamente 550 tCO₂e; no Cerrado, cerca de 110 tCO₂e/ha.",

            "Os principais padrões internacionais aceitos no mercado voluntário são: " +
            "Verra VCS (Verified Carbon Standard) — maior programa de certificação voluntária; " +
            "Gold Standard — foco em cobeneficios sociais e ODS; " +
            "ART/TREES — padrão específico para créditos jurisdicionais REDD+, " +
            "adotado pela Coalizão LEAF; " +
            "Cercarbono — programa latino-americano com atuação regional."
        );
    }
}
