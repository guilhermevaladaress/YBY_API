package com.yby.api.dto;

import com.yby.api.entity.enums.Semaforo;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record MunicipioDetalheDTO(
    Long id,
    String nome,
    String codigoIbge,
    BigDecimal areaHa,
    BigDecimal scorePrioridade,
    Semaforo semaforo,
    BigDecimal notaRisco,
    BigDecimal kpiRetorno,
    List<String> pendencias,
    OffsetDateTime ultimaAtualizacao,
    String geojsonPolygon
) {
}
