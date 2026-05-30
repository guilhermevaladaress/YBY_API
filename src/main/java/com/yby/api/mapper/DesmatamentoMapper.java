package com.yby.api.mapper;

import com.yby.api.dto.DesmatamentoDTO;
import com.yby.api.entity.Desmatamento;
import org.springframework.stereotype.Component;

@Component
public class DesmatamentoMapper {

    public DesmatamentoDTO toDTO(Desmatamento desmatamento) {
        return new DesmatamentoDTO(
            desmatamento.getId(),
            desmatamento.getMunicipio().getId(),
            desmatamento.getFonte().name(),
            desmatamento.getDataReferencia(),
            desmatamento.getAreaHa(),
            desmatamento.getBioma()
        );
    }
}
