package com.yby.api.integration.terrabrasilis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProdesDTO(
    @JsonProperty("municipio") String municipio,
    @JsonProperty("cod_municipio") String codMunicipio,
    @JsonProperty("estado") String estado,
    @JsonProperty("ano") Integer ano,
    @JsonProperty("area_km2") BigDecimal areaKm2,
    @JsonProperty("bioma") String bioma
) {
    public BigDecimal areaHa() {
        return areaKm2 == null ? BigDecimal.ZERO : areaKm2.multiply(new BigDecimal("100"));
    }
}
