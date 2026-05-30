package com.yby.api.mapper;

import com.yby.api.dto.CarbonoHistoricoDTO;
import com.yby.api.dto.CarbonoRegistroDTO;
import com.yby.api.entity.EmissaoCarbono;
import com.yby.api.repository.CarbonoHistoricoView;
import org.springframework.stereotype.Component;

@Component
public class CarbonoMapper {

    public CarbonoHistoricoDTO toHistoricoDTO(CarbonoHistoricoView view) {
        return new CarbonoHistoricoDTO(
            view.getMunicipioId(),
            view.getNome(),
            view.getCodigoIbge(),
            view.getEmissaoTotal(),
            view.getMediaDiaria()
        );
    }

    public CarbonoRegistroDTO toRegistroDTO(EmissaoCarbono emissao) {
        return new CarbonoRegistroDTO(
            emissao.getId(),
            emissao.getMunicipio().getId(),
            emissao.getDataReferencia(),
            emissao.getEmissaoTco2e(),
            emissao.getFonte(),
            emissao.getObservacao()
        );
    }
}
