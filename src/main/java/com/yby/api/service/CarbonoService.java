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
        // Sem filtro de datas, a pagina deve mostrar o ano mais recente COM dado (a serie SEEG e
        // anual e defasada): assim a consulta padrao nunca cai num intervalo vazio (ex.: futuro).
        LocalDate ultimaData = emissaoCarbonoRepository.findMaxDataReferencia();
        LocalDate fimPadrao = ultimaData != null ? ultimaData : LocalDate.now();
        LocalDate inicioPadrao = fimPadrao.withDayOfYear(1);

        LocalDate inicio = dataInicio == null ? inicioPadrao : dataInicio;
        LocalDate fim = dataFim == null ? fimPadrao : dataFim;
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
        LocalDate ultimaData = emissaoCarbonoRepository.findMaxDataReferencia();
        LocalDate fimPadrao = ultimaData != null ? ultimaData : LocalDate.now();
        return pdfReportService.gerarRelatorioCarbono(
            dataInicio == null ? fimPadrao.withDayOfYear(1) : dataInicio,
            dataFim == null ? fimPadrao : dataFim,
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
