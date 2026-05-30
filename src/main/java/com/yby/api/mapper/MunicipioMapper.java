package com.yby.api.mapper;

import com.yby.api.dto.MunicipioDetalheDTO;
import com.yby.api.dto.MunicipioRankingDTO;
import com.yby.api.dto.MunicipioUpdateDTO;
import com.yby.api.entity.Municipio;
import com.yby.api.entity.enums.Semaforo;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MunicipioMapper {

    public MunicipioRankingDTO toRankingDTO(Municipio municipio) {
        return new MunicipioRankingDTO(
            municipio.getId(),
            municipio.getNome(),
            municipio.getCodigoIbge(),
            municipio.getScorePrioridade(),
            toLower(municipio.getSemaforo()),
            municipio.getAreaHa(),
            municipio.getKpiRetorno()
        );
    }

    public MunicipioDetalheDTO toDetalheDTO(Municipio municipio) {
        return new MunicipioDetalheDTO(
            municipio.getId(),
            municipio.getNome(),
            municipio.getCodigoIbge(),
            municipio.getScorePrioridade(),
            toLower(municipio.getSemaforo()),
            municipio.getNotaRisco(),
            municipio.getKpiRetorno(),
            municipio.getAreaHa(),
            toPendencias(municipio.getPendenciasResumo()),
            municipio.getUltimaAtualizacao()
        );
    }

    public void applyUpdate(Municipio municipio, MunicipioUpdateDTO dto) {
        if (dto.nome() != null) {
            municipio.setNome(dto.nome());
        }
        if (dto.codigoIbge() != null) {
            municipio.setCodigoIbge(dto.codigoIbge());
        }
        if (dto.scorePrioridade() != null) {
            municipio.setScorePrioridade(dto.scorePrioridade());
        }
        if (dto.semaforo() != null) {
            municipio.setSemaforo(Semaforo.valueOf(dto.semaforo().toUpperCase()));
        }
        if (dto.areaHa() != null) {
            municipio.setAreaHa(dto.areaHa());
        }
        if (dto.notaRisco() != null) {
            municipio.setNotaRisco(dto.notaRisco());
        }
        if (dto.pendenciasResumo() != null) {
            municipio.setPendenciasResumo(dto.pendenciasResumo());
        }
        if (dto.geojsonPolygon() != null) {
            municipio.setGeojsonPolygon(dto.geojsonPolygon());
        }
    }

    private String toLower(Semaforo semaforo) {
        return semaforo.name().toLowerCase();
    }

    private List<String> toPendencias(String pendenciasResumo) {
        if (pendenciasResumo == null || pendenciasResumo.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(pendenciasResumo.split(";"))
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .toList();
    }
}
