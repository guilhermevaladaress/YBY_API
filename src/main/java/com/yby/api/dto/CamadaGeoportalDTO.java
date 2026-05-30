package com.yby.api.dto;

/**
 * Camada do Geoportal da SEPLAN-TO sugerida para o estudo de areas de carbono.
 *
 * @param codigo            identificador interno da camada
 * @param titulo            titulo legivel
 * @param tema              tema territorial (ex.: COBERTURA_VEGETAL, UNIDADE_CONSERVACAO)
 * @param typeName          nome WFS da camada no GeoServer (para consulta direta)
 * @param descricao         o que a camada representa e por que importa para carbono
 * @param relevanciaCarbono nivel de relevancia para projetos de carbono (ALTA, MEDIA, BAIXA)
 */
public record CamadaGeoportalDTO(
    String codigo,
    String titulo,
    String tema,
    String typeName,
    String descricao,
    String relevanciaCarbono
) {
}
