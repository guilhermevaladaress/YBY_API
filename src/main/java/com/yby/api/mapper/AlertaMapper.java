package com.yby.api.mapper;

import com.yby.api.dto.AlertaDTO;
import com.yby.api.entity.Alerta;
import org.springframework.stereotype.Component;

@Component
public class AlertaMapper {

    public AlertaDTO toDTO(Alerta alerta) {
        return new AlertaDTO(
            alerta.getId(),
            alerta.getMunicipio().getId(),
            alerta.getTipo().name(),
            alerta.getGravidade().name(),
            alerta.getDescricao(),
            alerta.getAcaoRecomendada(),
            alerta.getDataAlerta(),
            alerta.isAtivo()
        );
    }
}
