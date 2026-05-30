package com.yby.api.integration.ibge;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representacao minima de um municipio retornado pela API de Localidades do IBGE.
 *
 * <p>Endpoint: {@code GET /localidades/estados/{UF}/municipios}. O {@code id} e o
 * codigo IBGE de 7 digitos exigido pela RN-005.3 (municipios.codigo_ibge).</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IbgeMunicipioResponse(Long id, String nome) {
}
