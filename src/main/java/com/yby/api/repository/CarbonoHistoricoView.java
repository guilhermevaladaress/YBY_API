package com.yby.api.repository;

import java.math.BigDecimal;

public interface CarbonoHistoricoView {

    Long getMunicipioId();

    String getNome();

    String getCodigoIbge();

    BigDecimal getEmissaoTotal();

    BigDecimal getMediaDiaria();
}
