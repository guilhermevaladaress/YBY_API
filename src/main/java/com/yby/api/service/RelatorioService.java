package com.yby.api.service;

import com.yby.api.dto.RelatorioDTO;
import com.yby.api.dto.RelatorioUpsertDTO;
import com.yby.api.entity.Relatorio;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.mapper.RelatorioMapper;
import com.yby.api.repository.RelatorioRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatorioService {

    private final RelatorioRepository relatorioRepository;
    private final MunicipioService municipioService;
    private final AnaliseService analiseService;
    private final AuditService auditService;

    public RelatorioService(
        RelatorioRepository relatorioRepository,
        MunicipioService municipioService,
        AnaliseService analiseService,
        AuditService auditService
    ) {
        this.relatorioRepository = relatorioRepository;
        this.municipioService = municipioService;
        this.analiseService = analiseService;
        this.auditService = auditService;
    }

    public Page<RelatorioDTO> listar(Pageable pageable) {
        return relatorioRepository.findAllByOrderByCreatedAtDesc(pageable).map(RelatorioMapper::toDto);
    }

    public RelatorioDTO buscarPorMunicipio(Long municipioId) {
        municipioService.buscarMunicipio(municipioId);
        Relatorio relatorio = relatorioRepository.findTopByMunicipioIdOrderByDataReferenciaDesc(municipioId)
            .orElseGet(() -> gerarRelatorioAutomatico(municipioId));
        return RelatorioMapper.toDto(relatorio);
    }

    public List<RelatorioDTO> historicoPorMunicipio(Long municipioId) {
        municipioService.buscarMunicipio(municipioId);
        return relatorioRepository.findByMunicipioIdOrderByDataReferenciaDesc(municipioId)
            .stream()
            .map(RelatorioMapper::toDto)
            .toList();
    }

    @Transactional
    public RelatorioDTO criar(RelatorioUpsertDTO dto) {
        var municipio = municipioService.buscarMunicipio(dto.municipioId());

        Relatorio relatorio = new Relatorio();
        relatorio.setMunicipio(municipio);
        relatorio.setTitulo(dto.titulo());
        relatorio.setResumo(dto.resumo());
        relatorio.setRecomendacoes(dto.recomendacoes());
        relatorio.setDataReferencia(dto.dataReferencia());

        Relatorio saved = relatorioRepository.save(relatorio);
        auditService.registrarEscritaGestor("CREATE", "relatorios", String.valueOf(saved.getId()), dto);
        return RelatorioMapper.toDto(saved);
    }

    @Transactional
    public RelatorioDTO atualizar(Long id, RelatorioUpsertDTO dto) {
        Relatorio relatorio = relatorioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Relatorio nao encontrado"));

        var municipio = municipioService.buscarMunicipio(dto.municipioId());
        relatorio.setMunicipio(municipio);
        relatorio.setTitulo(dto.titulo());
        relatorio.setResumo(dto.resumo());
        relatorio.setRecomendacoes(dto.recomendacoes());
        relatorio.setDataReferencia(dto.dataReferencia());

        Relatorio saved = relatorioRepository.save(relatorio);
        auditService.registrarEscritaGestor("UPDATE", "relatorios", String.valueOf(saved.getId()), dto);
        return RelatorioMapper.toDto(saved);
    }

    @Transactional
    public void deletar(Long id) {
        Relatorio relatorio = relatorioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Relatorio nao encontrado"));
        relatorioRepository.delete(relatorio);
        auditService.registrarEscritaGestor("DELETE", "relatorios", String.valueOf(id), null);
    }

    @Transactional
    public void deletarPorMunicipio(Long municipioId) {
        municipioService.buscarMunicipio(municipioId);
        relatorioRepository.deleteByMunicipioId(municipioId);
        auditService.registrarEscritaGestor("DELETE", "relatorios", "municipio:" + municipioId, null);
    }

    private Relatorio gerarRelatorioAutomatico(Long municipioId) {
        var municipio = municipioService.buscarMunicipio(municipioId);
        var risco = analiseService.risco(municipioId);
        var retorno = analiseService.retorno(municipioId);
        var prontidao = analiseService.prontidao(municipioId);

        Relatorio relatorio = new Relatorio();
        relatorio.setMunicipio(municipio);
        relatorio.setTitulo("Relatorio automatico - " + municipio.getNome());
        relatorio.setResumo(
            "Nota de risco " + risco.notaRisco() + ", prontidao " + prontidao.semaforo().name() + ", retorno " + retorno.classificacao()
        );
        relatorio.setRecomendacoes(String.join("; ", prontidao.motivos()));
        relatorio.setDataReferencia(java.time.LocalDate.now());
        return relatorioRepository.save(relatorio);
    }
}
