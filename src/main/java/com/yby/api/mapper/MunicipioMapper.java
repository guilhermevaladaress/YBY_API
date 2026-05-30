package com.yby.api.mapper;

import com.yby.api.dto.MunicipioDTO;
import com.yby.api.dto.MunicipioDetalheDTO;
import com.yby.api.dto.MunicipioRankingDTO;
import com.yby.api.entity.Municipio;
import java.math.BigDecimal;
import java.util.List;

public final class MunicipioMapper {

    private MunicipioMapper() {
    }

    public static MunicipioDTO toDto(Municipio municipio, BigDecimal kpiRetorno) {
        return new MunicipioDTO(
            municipio.getId(),
            municipio.getNome(),
            municipio.getCodigoIbge(),
            municipio.getAreaHa(),
            municipio.getScorePrioridade(),
            municipio.getSemaforo(),
            kpiRetorno,
            municipio.getUltimaAtualizacao()
        );
    }

    public static MunicipioRankingDTO toRankingDto(Municipio municipio, BigDecimal kpiRetorno) {
        return new MunicipioRankingDTO(
            municipio.getId(),
            municipio.getNome(),
            municipio.getCodigoIbge(),
            municipio.getScorePrioridade(),
            municipio.getSemaforo(),
            municipio.getAreaHa(),
            kpiRetorno
        );
    }

    public static MunicipioDetalheDTO toDetalheDto(
        Municipio municipio,
        BigDecimal notaRisco,
        BigDecimal kpiRetorno,
        List<String> pendencias
    ) {
        return new MunicipioDetalheDTO(
            municipio.getId(),
            municipio.getNome(),
            municipio.getCodigoIbge(),
            municipio.getAreaHa(),
            municipio.getScorePrioridade(),
            municipio.getSemaforo(),
            notaRisco,
            kpiRetorno,
            pendencias,
            municipio.getUltimaAtualizacao(),
            municipio.getGeojsonPolygon()
        );
    }
}
