package com.yby.api.service;

import com.yby.api.dto.LinhaCreditoSafraDTO;
import com.yby.api.dto.SimulacaoSafraDTO;
import com.yby.api.dto.SimulacaoSafraDTO.SimulacaoSafraItemDTO;
import com.yby.api.dto.SimulacaoSafraRequestDTO;
import com.yby.api.entity.LinhaCreditoSafra;
import com.yby.api.entity.enums.ProgramaSafra;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.repository.LinhaCreditoSafraRepository;
import com.yby.api.service.inteligencia.AlgoritmoMetadata;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastro das linhas de credito do Plano Safra e simulacao de financiamento + carbono.
 *
 * <p>Inova ao cruzar o credito rural (RenovAgro/ABC+) com o potencial de geracao de credito
 * de carbono: para uma area informada, estima o valor financiavel, o custo de juros e as
 * toneladas de CO2e evitadas/sequestradas, alem da receita de carbono correspondente.</p>
 */
@Service
public class PlanoSafraService {

    private final LinhaCreditoSafraRepository repository;
    private final AuditService auditService;

    /** Valor de referencia financiavel por hectare quando o teto nao limita (R$/ha). */
    private static final BigDecimal VALOR_FINANCIAVEL_HA = new BigDecimal("8000");

    public PlanoSafraService(LinhaCreditoSafraRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    public List<LinhaCreditoSafraDTO> listar(boolean somenteAtivas) {
        List<LinhaCreditoSafra> linhas = somenteAtivas ? repository.findByAtivoTrue() : repository.findAll();
        return linhas.stream().map(this::toDTO).toList();
    }

    public LinhaCreditoSafraDTO buscar(Long id) {
        return toDTO(buscarEntidade(id));
    }

    @Transactional
    public LinhaCreditoSafraDTO criar(LinhaCreditoSafraDTO dto) {
        LinhaCreditoSafra linha = new LinhaCreditoSafra();
        aplicar(linha, dto);
        LinhaCreditoSafra saved = repository.save(linha);
        auditService.registrarEscritaGestor("CREATE", "linhas_credito_safra", String.valueOf(saved.getId()), dto);
        return toDTO(saved);
    }

    @Transactional
    public LinhaCreditoSafraDTO atualizar(Long id, LinhaCreditoSafraDTO dto) {
        LinhaCreditoSafra linha = buscarEntidade(id);
        aplicar(linha, dto);
        LinhaCreditoSafra saved = repository.save(linha);
        auditService.registrarEscritaGestor("UPDATE", "linhas_credito_safra", String.valueOf(saved.getId()), dto);
        return toDTO(saved);
    }

    @Transactional
    public void deletar(Long id) {
        LinhaCreditoSafra linha = buscarEntidade(id);
        repository.delete(linha);
        auditService.registrarEscritaGestor("DELETE", "linhas_credito_safra", String.valueOf(id), null);
    }

    /**
     * Simula o financiamento do Plano Safra para uma area, ordenado da menor taxa para a maior,
     * com o potencial de credito de carbono de cada linha.
     */
    public SimulacaoSafraDTO simular(SimulacaoSafraRequestDTO req) {
        List<LinhaCreditoSafra> elegiveis = new ArrayList<>(repository.findByAtivoTrue());
        if (req.programa() != null) {
            elegiveis.removeIf(l -> l.getPrograma() != req.programa());
        }
        if (req.somenteCarbono()) {
            elegiveis.removeIf(l -> !l.isExigePraticaCarbono());
        }
        elegiveis.sort(Comparator.comparing(LinhaCreditoSafra::getTaxaJurosAa));

        BigDecimal area = req.areaHa();
        BigDecimal financiavelBase = area.multiply(VALOR_FINANCIAVEL_HA);

        List<SimulacaoSafraItemDTO> itens = new ArrayList<>();
        BigDecimal totalFinanciavel = BigDecimal.ZERO;
        BigDecimal melhorTco2e = BigDecimal.ZERO;

        for (LinhaCreditoSafra l : elegiveis) {
            BigDecimal valor = financiavelBase;
            if (l.getTetoFinanciamento() != null && valor.compareTo(l.getTetoFinanciamento()) > 0) {
                valor = l.getTetoFinanciamento();
            }
            valor = valor.setScale(2, RoundingMode.HALF_UP);
            BigDecimal juros = valor.multiply(l.getTaxaJurosAa()).setScale(2, RoundingMode.HALF_UP);

            BigDecimal tco2e = BigDecimal.ZERO;
            if (l.isExigePraticaCarbono() && l.getFatorReducaoTco2eHa() != null) {
                tco2e = area.multiply(l.getFatorReducaoTco2eHa()).setScale(4, RoundingMode.HALF_UP);
                if (tco2e.compareTo(melhorTco2e) > 0) {
                    melhorTco2e = tco2e;
                }
            }
            if (valor.compareTo(totalFinanciavel) > 0) {
                totalFinanciavel = valor;
            }
            itens.add(new SimulacaoSafraItemDTO(l.getId(), l.getNome(), l.getPrograma().name(),
                l.getTaxaJurosAa(), valor, juros, tco2e));
        }

        BigDecimal receitaCarbono = null;
        if (req.precoTonelada() != null && melhorTco2e.compareTo(BigDecimal.ZERO) > 0) {
            receitaCarbono = melhorTco2e.multiply(req.precoTonelada()).setScale(2, RoundingMode.HALF_UP);
        }

        return new SimulacaoSafraDTO(area, itens, totalFinanciavel.setScale(2, RoundingMode.HALF_UP),
            melhorTco2e, receitaCarbono, AlgoritmoMetadata.VERSAO);
    }

    private void aplicar(LinhaCreditoSafra linha, LinhaCreditoSafraDTO dto) {
        linha.setNome(dto.nome());
        linha.setPrograma(dto.programa() == null ? ProgramaSafra.OUTRO : dto.programa());
        linha.setAnoSafra(dto.anoSafra());
        linha.setInstituicaoFinanceira(dto.instituicaoFinanceira());
        linha.setTaxaJurosAa(dto.taxaJurosAa());
        linha.setTetoFinanciamento(dto.tetoFinanciamento());
        linha.setPrazoMeses(dto.prazoMeses());
        linha.setCarenciaMeses(dto.carenciaMeses());
        linha.setExigePraticaCarbono(dto.exigePraticaCarbono());
        linha.setFatorReducaoTco2eHa(dto.fatorReducaoTco2eHa());
        linha.setDescricao(dto.descricao());
        linha.setAtivo(dto.ativo() == null ? true : dto.ativo());
    }

    private LinhaCreditoSafra buscarEntidade(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Linha de credito do Plano Safra nao encontrada"));
    }

    private LinhaCreditoSafraDTO toDTO(LinhaCreditoSafra l) {
        return new LinhaCreditoSafraDTO(l.getId(), l.getNome(), l.getPrograma(), l.getAnoSafra(),
            l.getInstituicaoFinanceira(), l.getTaxaJurosAa(), l.getTetoFinanciamento(), l.getPrazoMeses(),
            l.getCarenciaMeses(), l.isExigePraticaCarbono(), l.getFatorReducaoTco2eHa(), l.getDescricao(),
            l.isAtivo());
    }
}
