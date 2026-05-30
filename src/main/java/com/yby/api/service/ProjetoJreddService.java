package com.yby.api.service;

import com.yby.api.dto.MarcoProjetoDTO;
import com.yby.api.dto.ProjetoJreddDTO;
import com.yby.api.entity.MarcoProjeto;
import com.yby.api.entity.ProjetoJredd;
import com.yby.api.entity.enums.MarcoStatus;
import com.yby.api.entity.enums.ProjetoStatus;
import com.yby.api.exception.BusinessException;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.repository.MarcoProjetoRepository;
import com.yby.api.repository.ProjetoJreddRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestao de projetos JREDD+: CRUD de projetos (meta de carbono, orcamento, status) e de seus
 * marcos de execucao, com acompanhamento do percentual de conclusao.
 */
@Service
public class ProjetoJreddService {

    private final ProjetoJreddRepository projetoRepository;
    private final MarcoProjetoRepository marcoRepository;
    private final MunicipioService municipioService;
    private final AuditService auditService;

    public ProjetoJreddService(ProjetoJreddRepository projetoRepository,
                               MarcoProjetoRepository marcoRepository,
                               MunicipioService municipioService,
                               AuditService auditService) {
        this.projetoRepository = projetoRepository;
        this.marcoRepository = marcoRepository;
        this.municipioService = municipioService;
        this.auditService = auditService;
    }

    public Page<ProjetoJreddDTO> listar(Pageable pageable) {
        return projetoRepository.findAll(pageable).map(this::toDTO);
    }

    public ProjetoJreddDTO buscar(Long id) {
        return toDTO(buscarEntidade(id));
    }

    @Transactional
    public ProjetoJreddDTO criar(ProjetoJreddDTO dto) {
        ProjetoJredd projeto = new ProjetoJredd();
        aplicar(projeto, dto);
        ProjetoJredd saved = projetoRepository.save(projeto);
        auditService.registrarEscritaGestor("CREATE", "projetos_jredd", String.valueOf(saved.getId()), dto);
        return toDTO(saved);
    }

    @Transactional
    public ProjetoJreddDTO atualizar(Long id, ProjetoJreddDTO dto) {
        ProjetoJredd projeto = buscarEntidade(id);
        aplicar(projeto, dto);
        ProjetoJredd saved = projetoRepository.save(projeto);
        auditService.registrarEscritaGestor("UPDATE", "projetos_jredd", String.valueOf(saved.getId()), dto);
        return toDTO(saved);
    }

    @Transactional
    public void deletar(Long id) {
        ProjetoJredd projeto = buscarEntidade(id);
        projetoRepository.delete(projeto);
        auditService.registrarEscritaGestor("DELETE", "projetos_jredd", String.valueOf(id), projeto.getNome());
    }

    @Transactional
    public MarcoProjetoDTO adicionarMarco(Long projetoId, MarcoProjetoDTO dto) {
        ProjetoJredd projeto = buscarEntidade(projetoId);
        MarcoProjeto marco = new MarcoProjeto();
        marco.setProjeto(projeto);
        aplicarMarco(marco, dto);
        MarcoProjeto saved = marcoRepository.save(marco);
        auditService.registrarEscritaGestor("CREATE", "marcos_projeto", String.valueOf(saved.getId()), dto);
        return toMarcoDTO(saved);
    }

    @Transactional
    public MarcoProjetoDTO atualizarMarco(Long projetoId, Long marcoId, MarcoProjetoDTO dto) {
        MarcoProjeto marco = buscarMarco(projetoId, marcoId);
        aplicarMarco(marco, dto);
        MarcoProjeto saved = marcoRepository.save(marco);
        auditService.registrarEscritaGestor("UPDATE", "marcos_projeto", String.valueOf(saved.getId()), dto);
        return toMarcoDTO(saved);
    }

    @Transactional
    public void removerMarco(Long projetoId, Long marcoId) {
        MarcoProjeto marco = buscarMarco(projetoId, marcoId);
        marcoRepository.delete(marco);
        auditService.registrarEscritaGestor("DELETE", "marcos_projeto", String.valueOf(marcoId), projetoId);
    }

    private void aplicar(ProjetoJredd projeto, ProjetoJreddDTO dto) {
        projeto.setNome(dto.nome());
        projeto.setDescricao(dto.descricao());
        projeto.setMunicipio(dto.municipioId() == null ? null : municipioService.buscarMunicipio(dto.municipioId()));
        projeto.setStatus(dto.status() == null ? ProjetoStatus.PLANEJADO : dto.status());
        projeto.setDataInicio(dto.dataInicio());
        projeto.setDataFimPrevista(dto.dataFimPrevista());
        projeto.setMetaTco2e(dto.metaTco2e());
        projeto.setOrcamentoPrevisto(dto.orcamentoPrevisto());
    }

    private void aplicarMarco(MarcoProjeto marco, MarcoProjetoDTO dto) {
        marco.setTitulo(dto.titulo());
        marco.setDescricao(dto.descricao());
        marco.setDataPrevista(dto.dataPrevista());
        marco.setDataConclusao(dto.dataConclusao());
        marco.setStatus(dto.status() == null ? MarcoStatus.PENDENTE : dto.status());
        marco.setPercentualConclusao(dto.percentualConclusao() == null ? 0 : dto.percentualConclusao());
    }

    private ProjetoJredd buscarEntidade(Long id) {
        return projetoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Projeto JREDD+ nao encontrado"));
    }

    private MarcoProjeto buscarMarco(Long projetoId, Long marcoId) {
        MarcoProjeto marco = marcoRepository.findById(marcoId)
            .orElseThrow(() -> new ResourceNotFoundException("Marco nao encontrado"));
        if (!marco.getProjeto().getId().equals(projetoId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Marco nao pertence ao projeto informado");
        }
        return marco;
    }

    private ProjetoJreddDTO toDTO(ProjetoJredd p) {
        List<MarcoProjetoDTO> marcos = p.getMarcos().stream().map(this::toMarcoDTO).toList();
        Integer mediaConclusao = marcos.isEmpty() ? 0
            : (int) Math.round(p.getMarcos().stream()
                .mapToInt(m -> m.getPercentualConclusao() == null ? 0 : m.getPercentualConclusao())
                .average().orElse(0));
        return new ProjetoJreddDTO(
            p.getId(), p.getNome(), p.getDescricao(),
            p.getMunicipio() == null ? null : p.getMunicipio().getId(),
            p.getStatus(), p.getDataInicio(), p.getDataFimPrevista(),
            p.getMetaTco2e(), p.getOrcamentoPrevisto(), marcos, mediaConclusao);
    }

    private MarcoProjetoDTO toMarcoDTO(MarcoProjeto m) {
        return new MarcoProjetoDTO(
            m.getId(), m.getProjeto().getId(), m.getTitulo(), m.getDescricao(),
            m.getDataPrevista(), m.getDataConclusao(), m.getStatus(), m.getPercentualConclusao());
    }
}
