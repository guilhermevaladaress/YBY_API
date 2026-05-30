package com.yby.api.service;

import com.yby.api.dto.CarbonoHistoricoDTO;
import com.yby.api.dto.CarbonoRegistroDTO;
import com.yby.api.entity.EmissaoCarbono;
import com.yby.api.exception.BusinessException;
import com.yby.api.mapper.CarbonoMapper;
import com.yby.api.repository.EmissaoCarbonoRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CarbonoService {

    private final EmissaoCarbonoRepository emissaoCarbonoRepository;
    private final MunicipioService municipioService;
    private final CarbonoMapper carbonoMapper;
    private final PdfReportService pdfReportService;
    private final AuditService auditService;

    public CarbonoService(EmissaoCarbonoRepository emissaoCarbonoRepository,
                          MunicipioService municipioService,
                          CarbonoMapper carbonoMapper,
                          PdfReportService pdfReportService,
                          AuditService auditService) {
        this.emissaoCarbonoRepository = emissaoCarbonoRepository;
        this.municipioService = municipioService;
        this.carbonoMapper = carbonoMapper;
        this.pdfReportService = pdfReportService;
        this.auditService = auditService;
    }

    public List<CarbonoHistoricoDTO> historicoMenores(LocalDate dataInicio, LocalDate dataFim, Integer limite) {
        LocalDate inicio = dataInicio == null ? LocalDate.now().minusYears(1) : dataInicio;
        LocalDate fim = dataFim == null ? LocalDate.now() : dataFim;
        int limiteConsulta = (limite == null || limite <= 0) ? 20 : limite;

        if (inicio.isAfter(fim)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "dataInicio deve ser menor ou igual a dataFim");
        }

        return emissaoCarbonoRepository.findHistoricoMenoresEmissoes(inicio, fim, PageRequest.of(0, limiteConsulta))
            .stream()
            .map(carbonoMapper::toHistoricoDTO)
            .toList();
    }

    public byte[] relatorioPdfMenores(LocalDate dataInicio, LocalDate dataFim, Integer limite) {
        List<CarbonoHistoricoDTO> historico = historicoMenores(dataInicio, dataFim, limite);
        return pdfReportService.gerarRelatorioCarbono(
            dataInicio == null ? LocalDate.now().minusYears(1) : dataInicio,
            dataFim == null ? LocalDate.now() : dataFim,
            historico
        );
    }

    @Transactional
    public CarbonoRegistroDTO registrar(CarbonoRegistroDTO dto) {
        EmissaoCarbono emissao = new EmissaoCarbono();
        emissao.setMunicipio(municipioService.buscarMunicipio(dto.municipioId()));
        emissao.setDataReferencia(dto.dataReferencia());
        emissao.setEmissaoTco2e(dto.emissaoTco2e());
        emissao.setFonte(dto.fonte());
        emissao.setObservacao(dto.observacao());

        EmissaoCarbono saved = emissaoCarbonoRepository.save(emissao);
        auditService.registrarEscritaGestor("CREATE", "emissoes_carbono", String.valueOf(saved.getId()), dto);
        return carbonoMapper.toRegistroDTO(saved);
    }
}
