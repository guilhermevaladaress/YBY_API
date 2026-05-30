package com.yby.api.integration.inpe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FocoQueimadaDTO(
    @JsonProperty("id") String id,
    @JsonProperty("lat") Double latitude,
    @JsonProperty("lon") Double longitude,
    @JsonProperty("data_hora_gmt") LocalDate dataHora,
    @JsonProperty("municipio") String municipio,
    @JsonProperty("estado") String estado,
    @JsonProperty("bioma") String bioma,
    @JsonProperty("satelite") String satelite
) {}
