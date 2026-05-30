package com.yby.api.service.inteligencia;

import com.yby.api.dto.CarbonoEvitadoDTO;
import com.yby.api.entity.Municipio;
import com.yby.api.entity.enums.Bioma;
import com.yby.api.repository.DesmatamentoRepository;
import com.yby.api.service.MunicipioService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import org.springframework.stereotype.Service;

/**
 * Analise de Carbono Evitado (avoided emissions) - nucleo do JREDD+.
 *
 * <p>Compara o desmatamento de um ano com a linha de base (media dos anos anteriores). Os
 * hectares evitados sao convertidos em tCO2e por um fator de carbono por bioma (biomassa florestal
 * media) e valorados a preco de credito de carbono. Os fatores sao representativos e parametrizaveis;
 * em producao devem ser calibrados por inventario (ex.: MapBiomas/IPCC).</p>
 */
@Service
public class CarbonoEvitadoService {

    /** Fator de carbono por bioma (tCO2e por hectare de floresta convertida). */
    static final BigDecimal FATOR_AMAZONIA = new BigDecimal("550.00");
    static final BigDecimal FATOR_CERRADO = new BigDecimal("110.00");
    static final BigDecimal FATOR_PANTANAL = new BigDecimal("180.00");

    /** Preco padrao por tonelada de CO2e quando nao informado (R$). */
    static final BigDecimal PRECO_PADRAO = new BigDecimal("50.00");

    /** Numero de anos usados para compor a linha de base historica. */
    static final int ANOS_LINHA_BASE = 5;

    private final MunicipioService municipioService;
    private final DesmatamentoRepository desmatamentoRepository;

    public CarbonoEvitadoService(MunicipioService municipioService,
                                 DesmatamentoRepository desmatamentoRepository) {
        this.municipioService = municipioService;
        this.desmatamentoRepository = desmatamentoRepository;
    }

    public CarbonoEvitadoDTO avaliar(Long municipioId, Integer ano, BigDecimal preco) {
        Municipio municipio = municipioService.buscarMunicipio(municipioId);
        int anoRef = ano == null ? Year.now().getValue() : ano;
        Bioma bioma = municipio.getBioma() == null ? Bioma.CERRADO : municipio.getBioma();

        BigDecimal desmatamentoAno = areaNoAno(municipioId, anoRef);
        BigDecimal linhaBase = linhaBase(municipioId, anoRef);
        BigDecimal precoUsado = (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) ? PRECO_PADRAO : preco;

        return montar(municipioId, anoRef, bioma, linhaBase, desmatamentoAno, precoUsado);
    }

    /** Nucleo puro do calculo (testavel sem banco). */
    CarbonoEvitadoDTO montar(Long municipioId, int ano, Bioma bioma,
                             BigDecimal linhaBase, BigDecimal desmatamentoAno, BigDecimal preco) {
        BigDecimal base = linhaBase == null ? BigDecimal.ZERO : linhaBase;
        BigDecimal atual = desmatamentoAno == null ? BigDecimal.ZERO : desmatamentoAno;
        BigDecimal hectaresEvitados = base.subtract(atual).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        BigDecimal fator = fatorCarbono(bioma);
        BigDecimal tco2eEvitado = hectaresEvitados.multiply(fator).setScale(4, RoundingMode.HALF_UP);
        BigDecimal valor = tco2eEvitado.multiply(preco).setScale(2, RoundingMode.HALF_UP);

        return new CarbonoEvitadoDTO(municipioId, ano, bioma,
            base.setScale(2, RoundingMode.HALF_UP), atual.setScale(2, RoundingMode.HALF_UP),
            hectaresEvitados, fator, tco2eEvitado, preco, valor, AlgoritmoMetadata.VERSAO);
    }

    static BigDecimal fatorCarbono(Bioma bioma) {
        return switch (bioma == null ? Bioma.CERRADO : bioma) {
            case AMAZONIA -> FATOR_AMAZONIA;
            case PANTANAL -> FATOR_PANTANAL;
            case CERRADO -> FATOR_CERRADO;
        };
    }

    private BigDecimal linhaBase(Long municipioId, int anoRef) {
        BigDecimal soma = BigDecimal.ZERO;
        for (int a = anoRef - ANOS_LINHA_BASE; a < anoRef; a++) {
            soma = soma.add(areaNoAno(municipioId, a));
        }
        return soma.divide(BigDecimal.valueOf(ANOS_LINHA_BASE), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal areaNoAno(Long municipioId, int ano) {
        BigDecimal area = desmatamentoRepository.sumAreaByMunicipioAndPeriodo(
            municipioId, LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31));
        return area == null ? BigDecimal.ZERO : area;
    }
}
