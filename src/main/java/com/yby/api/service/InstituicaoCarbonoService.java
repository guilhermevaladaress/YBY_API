package com.yby.api.service;

import com.yby.api.dto.InstituicaoCarbonoDTO;
import com.yby.api.dto.MatchCompradorDTO;
import com.yby.api.dto.MatchCompradorDTO.MatchItemDTO;
import com.yby.api.dto.ProjecaoCarbonoDTO;
import com.yby.api.entity.InstituicaoCarbono;
import com.yby.api.entity.enums.InstituicaoTipo;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.repository.InstituicaoCarbonoRepository;
import com.yby.api.service.inteligencia.AlgoritmoMetadata;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastro de instituicoes do mercado de credito de carbono e matching com os creditos do estado.
 *
 * <p>Permite registrar compradores/certificadoras (Verra, Gold Standard, LEAF, marketplaces) e,
 * dado um credito cadastrado, indicar os compradores que maximizam a receita estimada.</p>
 */
@Service
public class InstituicaoCarbonoService {

    private final InstituicaoCarbonoRepository repository;
    private final CreditoCarbonoService creditoCarbonoService;
    private final AuditService auditService;

    public InstituicaoCarbonoService(InstituicaoCarbonoRepository repository,
                                     CreditoCarbonoService creditoCarbonoService,
                                     AuditService auditService) {
        this.repository = repository;
        this.creditoCarbonoService = creditoCarbonoService;
        this.auditService = auditService;
    }

    public List<InstituicaoCarbonoDTO> listar(InstituicaoTipo tipo, boolean somenteAtivas) {
        List<InstituicaoCarbono> instituicoes;
        if (tipo != null) {
            instituicoes = repository.findByTipoAndAtivoTrue(tipo);
        } else if (somenteAtivas) {
            instituicoes = repository.findByAtivoTrue();
        } else {
            instituicoes = repository.findAll();
        }
        return instituicoes.stream().map(this::toDTO).toList();
    }

    public InstituicaoCarbonoDTO buscar(Long id) {
        return toDTO(buscarEntidade(id));
    }

    @Transactional
    public InstituicaoCarbonoDTO criar(InstituicaoCarbonoDTO dto) {
        InstituicaoCarbono inst = new InstituicaoCarbono();
        aplicar(inst, dto);
        InstituicaoCarbono saved = repository.save(inst);
        auditService.registrarEscritaGestor("CREATE", "instituicoes_carbono", String.valueOf(saved.getId()), dto);
        return toDTO(saved);
    }

    @Transactional
    public InstituicaoCarbonoDTO atualizar(Long id, InstituicaoCarbonoDTO dto) {
        InstituicaoCarbono inst = buscarEntidade(id);
        aplicar(inst, dto);
        InstituicaoCarbono saved = repository.save(inst);
        auditService.registrarEscritaGestor("UPDATE", "instituicoes_carbono", String.valueOf(saved.getId()), dto);
        return toDTO(saved);
    }

    @Transactional
    public void deletar(Long id) {
        InstituicaoCarbono inst = buscarEntidade(id);
        repository.delete(inst);
        auditService.registrarEscritaGestor("DELETE", "instituicoes_carbono", String.valueOf(id), null);
    }

    /**
     * Faz o matching de um credito de carbono com os compradores ativos, ordenando
     * pela receita estimada (preco de referencia x tCO2e projetada).
     *
     * @param creditoId    credito a avaliar
     * @param somenteJredd quando verdadeiro, considera apenas instituicoes que compram JREDD+
     */
    public MatchCompradorDTO matchCompradores(Long creditoId, boolean somenteJredd) {
        ProjecaoCarbonoDTO projecao = creditoCarbonoService.projetar(creditoId);
        BigDecimal tco2eTotal = projecao.tco2eTotal() == null ? BigDecimal.ZERO : projecao.tco2eTotal();

        List<InstituicaoCarbono> candidatas = somenteJredd
            ? repository.findByCompraJreddTrueAndAtivoTrue()
            : repository.findByAtivoTrue();

        List<MatchItemDTO> compradores = candidatas.stream()
            .filter(i -> i.getPrecoReferenciaTonelada() != null)
            .map(i -> new MatchItemDTO(
                i.getId(), i.getNome(), i.getTipo().name(),
                i.getPadraoCertificacao() == null ? null : i.getPadraoCertificacao().name(),
                i.getPrecoReferenciaTonelada(), i.getMoeda(),
                tco2eTotal.multiply(i.getPrecoReferenciaTonelada()).setScale(2, RoundingMode.HALF_UP)))
            .sorted(Comparator.comparing(MatchItemDTO::receitaEstimada).reversed())
            .toList();

        return new MatchCompradorDTO(creditoId, projecao.municipioId(), tco2eTotal,
            compradores, AlgoritmoMetadata.VERSAO);
    }

    private void aplicar(InstituicaoCarbono inst, InstituicaoCarbonoDTO dto) {
        inst.setNome(dto.nome());
        inst.setTipo(dto.tipo());
        inst.setPadraoCertificacao(dto.padraoCertificacao());
        inst.setPais(dto.pais());
        inst.setPrecoReferenciaTonelada(dto.precoReferenciaTonelada());
        inst.setMoeda(dto.moeda() == null || dto.moeda().isBlank() ? "USD" : dto.moeda().toUpperCase());
        inst.setSiteUrl(dto.siteUrl());
        inst.setApiUrl(dto.apiUrl());
        inst.setContatoEmail(dto.contatoEmail());
        inst.setCompraJredd(dto.compraJredd());
        inst.setAtivo(dto.ativo() == null ? true : dto.ativo());
        inst.setObservacoes(dto.observacoes());
    }

    private InstituicaoCarbono buscarEntidade(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Instituicao de carbono nao encontrada"));
    }

    private InstituicaoCarbonoDTO toDTO(InstituicaoCarbono i) {
        return new InstituicaoCarbonoDTO(i.getId(), i.getNome(), i.getTipo(), i.getPadraoCertificacao(),
            i.getPais(), i.getPrecoReferenciaTonelada(), i.getMoeda(), i.getSiteUrl(), i.getApiUrl(),
            i.getContatoEmail(), i.isCompraJredd(), i.isAtivo(), i.getObservacoes());
    }
}
