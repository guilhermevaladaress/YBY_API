package com.yby.api.service;

import com.yby.api.dto.CreditoCarbonoDTO;
import com.yby.api.dto.ProjecaoAnualDTO;
import com.yby.api.dto.ProjecaoCarbonoDTO;
import com.yby.api.entity.CreditoCarbono;
import com.yby.api.entity.enums.CreditoStatus;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.repository.CreditoCarbonoRepository;
import com.yby.api.service.inteligencia.AlgoritmoMetadata;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastro e projecao financeira de credito rural de carbono.
 *
 * <p>O gestor registra a meta anual de creditos (tCO2e) por municipio e os parametros de preco;
 * o servico projeta a receita esperada de recebimento ao longo do horizonte informado e consolida
 * a projecao de todos os creditos para o dashboard.</p>
 */
@Service
public class CreditoCarbonoService {

    private final CreditoCarbonoRepository creditoCarbonoRepository;
    private final MunicipioService municipioService;
    private final AuditService auditService;

    public CreditoCarbonoService(CreditoCarbonoRepository creditoCarbonoRepository,
                                 MunicipioService municipioService,
                                 AuditService auditService) {
        this.creditoCarbonoRepository = creditoCarbonoRepository;
        this.municipioService = municipioService;
        this.auditService = auditService;
    }

    public Page<CreditoCarbonoDTO> listar(Pageable pageable) {
        return creditoCarbonoRepository.findAll(pageable).map(this::toDTO);
    }

    public CreditoCarbonoDTO buscar(Long id) {
        return toDTO(buscarEntidade(id));
    }

    public List<CreditoCarbonoDTO> listarPorMunicipio(Long municipioId) {
        return creditoCarbonoRepository.findByMunicipioId(municipioId).stream().map(this::toDTO).toList();
    }

    @Transactional
    public CreditoCarbonoDTO criar(CreditoCarbonoDTO dto) {
        CreditoCarbono credito = new CreditoCarbono();
        aplicar(credito, dto);
        CreditoCarbono saved = creditoCarbonoRepository.save(credito);
        auditService.registrarEscritaGestor("CREATE", "creditos_carbono", String.valueOf(saved.getId()), dto);
        return toDTO(saved);
    }

    @Transactional
    public CreditoCarbonoDTO atualizar(Long id, CreditoCarbonoDTO dto) {
        CreditoCarbono credito = buscarEntidade(id);
        aplicar(credito, dto);
        CreditoCarbono saved = creditoCarbonoRepository.save(credito);
        auditService.registrarEscritaGestor("UPDATE", "creditos_carbono", String.valueOf(saved.getId()), dto);
        return toDTO(saved);
    }

    @Transactional
    public void deletar(Long id) {
        CreditoCarbono credito = buscarEntidade(id);
        creditoCarbonoRepository.delete(credito);
        auditService.registrarEscritaGestor("DELETE", "creditos_carbono", String.valueOf(id), credito.getMunicipio().getId());
    }

    /** Projecao financeira ano a ano de um unico credito. */
    public ProjecaoCarbonoDTO projetar(Long id) {
        CreditoCarbono credito = buscarEntidade(id);
        List<ProjecaoAnualDTO> itens = projetarItens(credito);
        BigDecimal tco2eTotal = somar(itens, ProjecaoAnualDTO::toneladasProjetadas);
        BigDecimal receitaTotal = somar(itens, ProjecaoAnualDTO::receitaProjetadaReais);
        return new ProjecaoCarbonoDTO(credito.getId(), credito.getMunicipio().getId(), credito.getAnoBase(),
            itens, tco2eTotal, receitaTotal, AlgoritmoMetadata.VERSAO);
    }

    /**
     * Projecao consolidada de todos os creditos para o dashboard.
     *
     * @param ano         se informado, mantem apenas os anos &gt;= ano
     * @param municipioId se informado, restringe aos creditos do municipio
     */
    public ProjecaoCarbonoDTO projetarConsolidado(Integer ano, Long municipioId) {
        List<CreditoCarbono> creditos = municipioId == null
            ? creditoCarbonoRepository.findAll()
            : creditoCarbonoRepository.findByMunicipioId(municipioId);

        TreeMap<Integer, BigDecimal[]> porAno = new TreeMap<>();
        for (CreditoCarbono credito : creditos) {
            if (credito.getStatus() == CreditoStatus.CANCELADO) {
                continue;
            }
            for (ProjecaoAnualDTO item : projetarItens(credito)) {
                if (ano != null && item.ano() < ano) {
                    continue;
                }
                BigDecimal[] acc = porAno.computeIfAbsent(item.ano(),
                    k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                acc[0] = acc[0].add(item.toneladasProjetadas());
                acc[1] = acc[1].add(item.receitaProjetadaReais());
            }
        }

        List<ProjecaoAnualDTO> itens = new ArrayList<>();
        porAno.forEach((anoItem, acc) ->
            itens.add(new ProjecaoAnualDTO(anoItem, acc[0], null, acc[1])));

        BigDecimal tco2eTotal = somar(itens, ProjecaoAnualDTO::toneladasProjetadas);
        BigDecimal receitaTotal = somar(itens, ProjecaoAnualDTO::receitaProjetadaReais);
        Integer anoBase = itens.isEmpty() ? null : itens.getFirst().ano();
        return new ProjecaoCarbonoDTO(null, municipioId, anoBase, itens, tco2eTotal, receitaTotal,
            AlgoritmoMetadata.VERSAO);
    }

    /** Nucleo puro da projecao de um credito (testavel sem banco). */
    List<ProjecaoAnualDTO> projetarItens(CreditoCarbono credito) {
        BigDecimal cumprimento = credito.getPercentualCumprimentoMeta() == null
            ? BigDecimal.ONE : credito.getPercentualCumprimentoMeta();
        BigDecimal taxa = credito.getTaxaCrescimentoPreco() == null
            ? BigDecimal.ZERO : credito.getTaxaCrescimentoPreco();
        BigDecimal toneladas = credito.getMetaTco2eAno().multiply(cumprimento).setScale(4, RoundingMode.HALF_UP);

        List<ProjecaoAnualDTO> itens = new ArrayList<>();
        for (int n = 0; n < credito.getHorizonteAnos(); n++) {
            BigDecimal fatorPreco = BigDecimal.ONE.add(taxa).pow(n);
            BigDecimal preco = credito.getPrecoTonelada().multiply(fatorPreco).setScale(4, RoundingMode.HALF_UP);
            BigDecimal receita = toneladas.multiply(preco).setScale(2, RoundingMode.HALF_UP);
            itens.add(new ProjecaoAnualDTO(credito.getAnoBase() + n, toneladas, preco, receita));
        }
        return itens;
    }

    private BigDecimal somar(List<ProjecaoAnualDTO> itens,
                            java.util.function.Function<ProjecaoAnualDTO, BigDecimal> extrator) {
        return itens.stream().map(extrator).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void aplicar(CreditoCarbono credito, CreditoCarbonoDTO dto) {
        credito.setMunicipio(municipioService.buscarMunicipio(dto.municipioId()));
        credito.setAnoBase(dto.anoBase());
        credito.setMetaTco2eAno(dto.metaTco2eAno());
        credito.setPrecoTonelada(dto.precoTonelada());
        credito.setTaxaCrescimentoPreco(dto.taxaCrescimentoPreco() == null ? BigDecimal.ZERO : dto.taxaCrescimentoPreco());
        credito.setPercentualCumprimentoMeta(
            dto.percentualCumprimentoMeta() == null ? BigDecimal.ONE : dto.percentualCumprimentoMeta());
        credito.setHorizonteAnos(dto.horizonteAnos());
        credito.setDescricao(dto.descricao());
        credito.setStatus(dto.status() == null ? CreditoStatus.PLANEJADO : dto.status());
    }

    private CreditoCarbono buscarEntidade(Long id) {
        return creditoCarbonoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Credito de carbono nao encontrado"));
    }

    private CreditoCarbonoDTO toDTO(CreditoCarbono c) {
        return new CreditoCarbonoDTO(
            c.getId(), c.getMunicipio().getId(), c.getAnoBase(), c.getMetaTco2eAno(), c.getPrecoTonelada(),
            c.getTaxaCrescimentoPreco(), c.getPercentualCumprimentoMeta(), c.getHorizonteAnos(),
            c.getDescricao(), c.getStatus());
    }
}
