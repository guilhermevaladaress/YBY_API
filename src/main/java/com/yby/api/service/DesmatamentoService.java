package com.yby.api.service;

import com.yby.api.dto.DesmatamentoDTO;
import com.yby.api.dto.DesmatamentoResumoDTO;
import com.yby.api.dto.DesmatamentoResumoItemDTO;
import com.yby.api.dto.ImportJobStatusDTO;
import com.yby.api.entity.ImportJob;
import com.yby.api.entity.enums.FonteDesmatamento;
import com.yby.api.entity.enums.ImportJobStatus;
import com.yby.api.exception.BusinessException;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.mapper.DesmatamentoMapper;
import com.yby.api.repository.DesmatamentoRepository;
import com.yby.api.repository.ImportJobRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DesmatamentoService {

    private final DesmatamentoRepository desmatamentoRepository;
    private final ImportJobRepository importJobRepository;
    private final DesmatamentoMapper desmatamentoMapper;

    public DesmatamentoService(DesmatamentoRepository desmatamentoRepository,
                               ImportJobRepository importJobRepository,
                               DesmatamentoMapper desmatamentoMapper) {
        this.desmatamentoRepository = desmatamentoRepository;
        this.importJobRepository = importJobRepository;
        this.desmatamentoMapper = desmatamentoMapper;
    }

    public List<DesmatamentoDTO> historico(Long municipioId, String fonte, LocalDate dataInicio, LocalDate dataFim) {
        LocalDate inicio = dataInicio == null ? LocalDate.now().minusYears(5) : dataInicio;
        LocalDate fim = dataFim == null ? LocalDate.now() : dataFim;

        if (inicio.isAfter(fim)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "dataInicio deve ser menor ou igual a dataFim");
        }

        if (fonte == null || fonte.isBlank() || "ALL".equalsIgnoreCase(fonte)) {
            return desmatamentoRepository.findByMunicipioIdAndDataReferenciaBetween(municipioId, inicio, fim)
                .stream()
                .map(desmatamentoMapper::toDTO)
                .toList();
        }

        FonteDesmatamento fonteEnum = FonteDesmatamento.valueOf(fonte.toUpperCase());
        return desmatamentoRepository.findByMunicipioIdAndFonteAndDataReferenciaBetween(municipioId, fonteEnum, inicio, fim)
            .stream()
            .map(desmatamentoMapper::toDTO)
            .toList();
    }

    public DesmatamentoResumoDTO resumo(Integer ano) {
        int anoAtual = Year.now().getValue();
        if (ano == null || ano < 1900 || ano > anoAtual + 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ano invalido");
        }

        BigDecimal total = desmatamentoRepository.findTotalByAno(ano);
        List<DesmatamentoResumoItemDTO> itens = desmatamentoRepository.findResumoByAno(ano)
            .stream()
            .map(this::mapResumoItem)
            .toList();

        return new DesmatamentoResumoDTO(ano, total, itens);
    }

    @Transactional
    public Long iniciarImportacao() {
        ImportJob job = new ImportJob();
        job.setTipo("DESMATAMENTO");
        job.setStatus(ImportJobStatus.PROCESSANDO);
        job.setRegistrosInseridos(0);
        ImportJob saved = importJobRepository.save(job);

        CompletableFuture.runAsync(() -> concluirImportacaoMock(saved.getId()));
        return saved.getId();
    }

    public ImportJobStatusDTO statusImportacao(Long jobId) {
        ImportJob job = importJobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job de importacao nao encontrado"));

        return new ImportJobStatusDTO(
            job.getId(),
            job.getStatus().name(),
            job.getRegistrosInseridos(),
            parseErros(job.getErros()),
            job.getFinalizadoEm()
        );
    }

    @Transactional
    void concluirImportacaoMock(Long jobId) {
        ImportJob job = importJobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job de importacao nao encontrado"));

        job.setStatus(ImportJobStatus.CONCLUIDO);
        job.setRegistrosInseridos(0);
        job.setErros("[]");
        job.setFinalizadoEm(OffsetDateTime.now());
        importJobRepository.save(job);
    }

    private DesmatamentoResumoItemDTO mapResumoItem(Object[] row) {
        String bioma = row[0] == null ? "N/A" : String.valueOf(row[0]);
        String semaforo = row[1] == null ? "N/A" : String.valueOf(row[1]).toLowerCase();
        BigDecimal areaTotal = row[2] == null ? BigDecimal.ZERO : (BigDecimal) row[2];
        return new DesmatamentoResumoItemDTO(bioma, semaforo, areaTotal);
    }

    private List<String> parseErros(String erros) {
        if (erros == null || erros.isBlank() || "[]".equals(erros)) {
            return Collections.emptyList();
        }
        return List.of(erros);
    }
}
