package com.yby.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Area sugerida para estudo de carbono, cruzando os dados locais do municipio com as
 * camadas territoriais do Geoportal da SEPLAN-TO.
 *
 * @param municipioId        municipio analisado
 * @param nome               nome do municipio
 * @param codigoIbge         codigo IBGE
 * @param bioma              bioma predominante
 * @param areaHa             area territorial (ha)
 * @param scorePrioridade    score de prioridade ambiental
 * @param semaforo           situacao (VERDE/AMARELO/VERMELHO)
 * @param potencialTco2eAno  potencial estimado de credito de carbono (tCO2e/ano)
 * @param justificativa      por que a area foi sugerida
 * @param camadasRecomendadas camadas do geoportal a consultar para o estudo
 */
public record AreaCarbonoEstudoDTO(
    Long municipioId,
    String nome,
    String codigoIbge,
    String bioma,
    BigDecimal areaHa,
    BigDecimal scorePrioridade,
    String semaforo,
    BigDecimal potencialTco2eAno,
    String justificativa,
    List<CamadaGeoportalDTO> camadasRecomendadas
) {
}
