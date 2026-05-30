package com.yby.api.service;

import com.yby.api.dto.IndicadorAnualDTO;
import com.yby.api.dto.KpiDTO;
import com.yby.api.entity.Indicador;
import com.yby.api.entity.Municipio;
import com.yby.api.exception.BusinessException;
import com.yby.api.mapper.IndicadorMapper;
import com.yby.api.repository.IndicadorRepository;
import com.yby.api.repository.MunicipioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IndicadorService {

    private final IndicadorRepository indicadorRepository;
    private final MunicipioService municipioService;
    private final MunicipioRepository municipioRepository;
    private final IndicadorMapper indicadorMapper;
    private final AuditService auditService;

    public IndicadorService(IndicadorRepository indicadorRepository,
                            MunicipioService municipioService,
                            MunicipioRepository municipioRepository,
                            IndicadorMapper indicadorMapper,
                            AuditService auditService) {
        this.indicadorRepository = indicadorRepository;
        this.municipioService = municipioService;
        this.municipioRepository = municipioRepository;
        this.indicadorMapper = indicadorMapper;
        this.auditService = auditService;
    }

    public KpiDTO kpi(Long municipioId, Integer anoInicio, Integer anoFim) {
        validarPeriodo(anoInicio, anoFim);

        List<Object[]> rows = indicadorRepository.aggregateByMunicipioAndPeriodo(municipioId, anoInicio, anoFim);
        BigDecimal gasto = BigDecimal.ZERO;
        BigDecimal resultado = BigDecimal.ZERO;

        if (!rows.isEmpty()) {
            Object[] row = rows.getFirst();
            gasto = row[0] == null ? BigDecimal.ZERO : (BigDecimal) row[0];
            resultado = row[1] == null ? BigDecimal.ZERO : (BigDecimal) row[1];
        }

        BigDecimal kpi = safeKpi(resultado, gasto);
        return new KpiDTO(municipioId, anoInicio, anoFim, gasto, resultado, kpi);
    }

    public List<IndicadorAnualDTO> historico(Long municipioId, Integer anoInicio, Integer anoFim) {
        validarPeriodo(anoInicio, anoFim);
        return indicadorRepository.findByMunicipioIdAndAnoBetweenOrderByAnoAsc(municipioId, anoInicio, anoFim)
            .stream()
            .map(indicadorMapper::toDTO)
            .toList();
    }

    @Transactional
    public IndicadorAnualDTO upsert(IndicadorAnualDTO dto) {
        Municipio municipio = municipioService.buscarMunicipio(dto.municipioId());
        Indicador indicador = indicadorRepository.findByMunicipioIdAndAno(dto.municipioId(), dto.ano())
            .orElseGet(Indicador::new);

        indicador.setMunicipio(municipio);
        indicadorMapper.apply(indicador, dto);
        Indicador saved = indicadorRepository.save(indicador);

        atualizarConsolidadoMunicipio(municipio, saved);
        auditService.registrarEscritaGestor("UPSERT", "indicadores", String.valueOf(saved.getId()), dto);
        return indicadorMapper.toDTO(saved);
    }

    private void atualizarConsolidadoMunicipio(Municipio municipio, Indicador indicador) {
        municipio.setGastoPublico(indicador.getGastoPublico());
        municipio.setResultadoAmbiental(indicador.getResultadoAmbiental());
        municipio.setKpiRetorno(safeKpi(indicador.getResultadoAmbiental(), indicador.getGastoPublico()));
        municipioRepository.save(municipio);
    }

    private BigDecimal safeKpi(BigDecimal resultadoAmbiental, BigDecimal gastoPublico) {
        if (gastoPublico == null || gastoPublico.compareTo(BigDecimal.ZERO) <= 0 || resultadoAmbiental == null) {
            return null;
        }
        return resultadoAmbiental.divide(gastoPublico, 6, RoundingMode.HALF_UP);
    }

    private void validarPeriodo(Integer anoInicio, Integer anoFim) {
        if (anoInicio == null || anoFim == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "anoInicio e anoFim sao obrigatorios");
        }
        if (anoInicio > anoFim) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "anoInicio deve ser menor ou igual a anoFim");
        }
        int anoAtual = Year.now().getValue();
        if (anoInicio < 1900 || anoFim > anoAtual + 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "intervalo de anos invalido");
        }
    }
}
