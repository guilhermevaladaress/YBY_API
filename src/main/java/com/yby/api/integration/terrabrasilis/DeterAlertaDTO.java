package com.yby.api.integration.terrabrasilis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DeterAlertaDTO(
    @JsonProperty("gid") String id,
    @JsonProperty("municipio") String municipio,
    @JsonProperty("estado") String estado,
    @JsonProperty("bioma") String bioma,
    @JsonProperty("area_km2") BigDecimal areaKm2,
    @JsonProperty("view_date") LocalDate dataDeteccao,
    @JsonProperty("classname") String classe
) {
    public BigDecimal areaHa() {
        return areaKm2 == null ? BigDecimal.ZERO : areaKm2.multiply(new BigDecimal("100"));
    }
}
