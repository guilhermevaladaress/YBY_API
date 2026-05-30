package com.yby.api.mapper;

import com.yby.api.dto.MunicipioDTO;
import com.yby.api.dto.MunicipioDetalheDTO;
import com.yby.api.dto.MunicipioRankingDTO;
import com.yby.api.dto.MunicipioUpdateDTO;
import com.yby.api.dto.MunicipioUpsertDTO;
import com.yby.api.entity.Municipio;
import com.yby.api.entity.enums.Semaforo;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MunicipioMapper {

    public MunicipioDTO toDTO(Municipio municipio, BigDecimal kpiRetorno) {
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

    public MunicipioRankingDTO toRankingDTO(Municipio municipio, BigDecimal kpiRetorno) {
        return new MunicipioRankingDTO(
            municipio.getId(),
            municipio.getNome(),
            municipio.getCodigoIbge(),
            municipio.getScorePrioridade(),
            toLower(municipio.getSemaforo()),
            municipio.getAreaHa(),
            kpiRetorno
        );
    }

    public MunicipioDetalheDTO toDetalheDTO(Municipio municipio, BigDecimal notaRisco,
                                            BigDecimal kpiRetorno, List<String> pendencias) {
        return new MunicipioDetalheDTO(
            municipio.getId(),
            municipio.getNome(),
            municipio.getCodigoIbge(),
            municipio.getScorePrioridade(),
            toLower(municipio.getSemaforo()),
            notaRisco,
            kpiRetorno,
            municipio.getAreaHa(),
            pendencias == null ? toPendencias(municipio.getPendenciasResumo()) : pendencias,
            municipio.getUltimaAtualizacao()
        );
    }

    public void applyUpsert(Municipio municipio, MunicipioUpsertDTO dto) {
        municipio.setNome(dto.nome().trim());
        municipio.setCodigoIbge(dto.codigoIbge().trim());
        municipio.setAreaHa(dto.areaHa());
        municipio.setScorePrioridade(dto.scorePrioridade());
        if (dto.semaforo() != null) {
            municipio.setSemaforo(dto.semaforo());
        }
        municipio.setGeojsonPolygon(dto.geojsonPolygon());
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
