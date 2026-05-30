package com.yby.api.mapper;

import com.yby.api.dto.IndicadorAnualDTO;
import com.yby.api.entity.Indicador;
import org.springframework.stereotype.Component;

@Component
public class IndicadorMapper {

    public IndicadorAnualDTO toDTO(Indicador indicador) {
        return new IndicadorAnualDTO(
            indicador.getId(),
            indicador.getMunicipio().getId(),
            indicador.getAno(),
            indicador.getGastoPublico(),
            indicador.getResultadoAmbiental(),
            indicador.getDesmatamentoRecenteFactor(),
            indicador.getEficienciaGastoFactor(),
            indicador.getIrregularidadesCarFactor(),
            indicador.getAreaElegivelFactor()
        );
    }

    public void apply(Indicador indicador, IndicadorAnualDTO dto) {
        indicador.setAno(dto.ano());
        indicador.setGastoPublico(dto.gastoPublico());
        indicador.setResultadoAmbiental(dto.resultadoAmbiental());
        indicador.setDesmatamentoRecenteFactor(dto.desmatamentoRecenteFactor());
        indicador.setEficienciaGastoFactor(dto.eficienciaGastoFactor());
        indicador.setIrregularidadesCarFactor(dto.irregularidadesCarFactor());
        indicador.setAreaElegivelFactor(dto.areaElegivelFactor());
    }
}
