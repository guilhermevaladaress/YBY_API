package com.yby.api.mapper;

import com.yby.api.dto.RelatorioDTO;
import com.yby.api.entity.Relatorio;

public final class RelatorioMapper {

    private RelatorioMapper() {
    }

    public static RelatorioDTO toDto(Relatorio relatorio) {
        return new RelatorioDTO(
            relatorio.getId(),
            relatorio.getMunicipio().getId(),
            relatorio.getMunicipio().getNome(),
            relatorio.getTitulo(),
            relatorio.getResumo(),
            relatorio.getRecomendacoes(),
            relatorio.getDataReferencia(),
            relatorio.getCreatedAt(),
            relatorio.getUpdatedAt()
        );
    }
}
