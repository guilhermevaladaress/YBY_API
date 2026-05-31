package com.yby.api.service;

import com.yby.api.dto.CotacaoDolarDTO;
import com.yby.api.dto.CreditoCarbonoDTO;
import com.yby.api.dto.GraficoProjecaoDTO;
import com.yby.api.dto.GraficoProjecaoDTO.SerieDTO;
import com.yby.api.dto.ProjecaoAnualDTO;
import com.yby.api.dto.ProjecaoCarbonoDTO;
import com.yby.api.entity.CreditoCarbono;
import com.yby.api.entity.enums.CreditoStatus;
import com.yby.api.exception.ResourceNotFoundException;
import com.yby.api.repository.CreditoCarbonoRepository;
import com.yby.api.service.inteligencia.AlgoritmoMetadata;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastro e projeção financeira de crédito rural de carbono.
 *
 * <p>O preço por tonelada é armazenado em USD (padrão do mercado voluntário global).
 * A receita em reais é calculada multiplicando a receita em USD pela cotação USD/BRL
 * da data de referência informada pelo usuário (ou cotação do dia se não informada).</p>
 *
 * <p>Fórmula de projeção para o ano N a partir do ano base:<br>
 * {@code toneladas = meta × cumprimento}<br>
 * {@code precoUsdN = precoBaseUsd × (1 + taxaCrescimento)^N}<br>
 * {@code receitaUsdN = toneladas × precoUsdN}<br>
 * {@code receitaBrlN = receitaUsdN × cotacaoUsdBrl}</p>
 */
@Service
public class CreditoCarbonoService {

    private final CreditoCarbonoRepository creditoCarbonoRepository;
    private final MunicipioService municipioService;
    private final AuditService auditService;
    private final CotacaoDolarService cotacaoDolarService;

    public CreditoCarbonoService(CreditoCarbonoRepository creditoCarbonoRepository,
                                 MunicipioService municipioService,
                                 AuditService auditService,
                                 CotacaoDolarService cotacaoDolarService) {
        this.creditoCarbonoRepository = creditoCarbonoRepository;
        this.municipioService = municipioService;
        this.auditService = auditService;
        this.cotacaoDolarService = cotacaoDolarService;
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

    /**
     * Projeção financeira ano a ano de um único crédito.
     *
     * @param id                  id do crédito
     * @param dataReferenciaCotacao data usada para buscar a cotação USD/BRL; hoje se nula
     */
    public ProjecaoCarbonoDTO projetar(Long id, LocalDate dataReferenciaCotacao) {
        CreditoCarbono credito = buscarEntidade(id);
        CotacaoDolarDTO cotacao = cotacaoDolarService.buscar(dataReferenciaCotacao);
        List<ProjecaoAnualDTO> itens = projetarItens(credito, cotacao.taxaUsdBrl());
        BigDecimal tco2eTotal = somarTco2e(itens);
        BigDecimal receitaTotalUsd = somarUsd(itens);
        BigDecimal receitaTotalReais = somarReais(itens);
        GraficoProjecaoDTO grafico = construirGrafico(itens);
        return new ProjecaoCarbonoDTO(
            credito.getId(), credito.getMunicipio().getId(), credito.getAnoBase(),
            itens, tco2eTotal, receitaTotalUsd, receitaTotalReais,
            cotacao, grafico, AlgoritmoMetadata.VERSAO);
    }

    /**
     * Projeção consolidada de todos os créditos para o dashboard.
     *
     * @param ano                   se informado, mantém apenas os anos &gt;= ano
     * @param municipioId           se informado, restringe aos créditos do município
     * @param dataReferenciaCotacao data de referência para cotação USD/BRL
     */
    public ProjecaoCarbonoDTO projetarConsolidado(Integer ano, Long municipioId,
                                                   LocalDate dataReferenciaCotacao) {
        CotacaoDolarDTO cotacao = cotacaoDolarService.buscar(dataReferenciaCotacao);
        return projetarConsolidadoComCotacao(ano, municipioId, cotacao);
    }

    /**
     * Variante interna que reutiliza uma cotação já obtida (evita chamadas redundantes).
     */
    ProjecaoCarbonoDTO projetarConsolidadoComCotacao(Integer ano, Long municipioId,
                                                      CotacaoDolarDTO cotacao) {
        List<CreditoCarbono> creditos = municipioId == null
            ? creditoCarbonoRepository.findAll()
            : creditoCarbonoRepository.findByMunicipioId(municipioId);

        TreeMap<Integer, BigDecimal[]> porAno = new TreeMap<>();
        for (CreditoCarbono credito : creditos) {
            if (credito.getStatus() == CreditoStatus.CANCELADO) {
                continue;
            }
            for (ProjecaoAnualDTO item : projetarItens(credito, cotacao.taxaUsdBrl())) {
                if (ano != null && item.ano() < ano) {
                    continue;
                }
                // acc[0]=tco2e, acc[1]=receitaUsd, acc[2]=receitaBrl
                BigDecimal[] acc = porAno.computeIfAbsent(item.ano(),
                    k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
                acc[0] = acc[0].add(item.toneladasProjetadas());
                acc[1] = acc[1].add(item.receitaProjetadaUsd());
                acc[2] = acc[2].add(item.receitaProjetadaReais());
            }
        }

        List<ProjecaoAnualDTO> itens = new ArrayList<>();
        porAno.forEach((anoItem, acc) ->
            itens.add(new ProjecaoAnualDTO(anoItem, acc[0], null, null, acc[1], acc[2])));

        BigDecimal tco2eTotal = somarTco2e(itens);
        BigDecimal receitaTotalUsd = somarUsd(itens);
        BigDecimal receitaTotalReais = somarReais(itens);
        Integer anoBase = itens.isEmpty() ? null : itens.getFirst().ano();
        GraficoProjecaoDTO grafico = construirGrafico(itens);
        return new ProjecaoCarbonoDTO(null, municipioId, anoBase, itens,
            tco2eTotal, receitaTotalUsd, receitaTotalReais,
            cotacao, grafico, AlgoritmoMetadata.VERSAO);
    }

    /**
     * Núcleo puro da projeção de um crédito (testável sem banco).
     *
     * <p>O preço base em USD cresce anualmente pela taxa de crescimento e é convertido para
     * reais pela cotação informada. As toneladas são constantes ao longo do horizonte.</p>
     *
     * @param credito      entidade do crédito com os parâmetros de projeção
     * @param taxaUsdBrl   cotação USD/BRL a aplicar em todos os anos
     */
    List<ProjecaoAnualDTO> projetarItens(CreditoCarbono credito, BigDecimal taxaUsdBrl) {
        BigDecimal cumprimento = credito.getPercentualCumprimentoMeta() == null
            ? BigDecimal.ONE : credito.getPercentualCumprimentoMeta();
        BigDecimal taxa = credito.getTaxaCrescimentoPreco() == null
            ? BigDecimal.ZERO : credito.getTaxaCrescimentoPreco();
        BigDecimal taxa_usd_brl = taxaUsdBrl == null
            ? CotacaoDolarService.TAXA_FALLBACK : taxaUsdBrl;

        BigDecimal toneladas = credito.getMetaTco2eAno()
            .multiply(cumprimento).setScale(4, RoundingMode.HALF_UP);

        List<ProjecaoAnualDTO> itens = new ArrayList<>();
        for (int n = 0; n < credito.getHorizonteAnos(); n++) {
            BigDecimal fatorPreco = BigDecimal.ONE.add(taxa).pow(n);
            BigDecimal precoUsd = credito.getPrecoTonelada()
                .multiply(fatorPreco).setScale(4, RoundingMode.HALF_UP);
            BigDecimal precoReais = precoUsd
                .multiply(taxa_usd_brl).setScale(4, RoundingMode.HALF_UP);
            BigDecimal receitaUsd = toneladas
                .multiply(precoUsd).setScale(2, RoundingMode.HALF_UP);
            BigDecimal receitaReais = toneladas
                .multiply(precoReais).setScale(2, RoundingMode.HALF_UP);
            itens.add(new ProjecaoAnualDTO(
                credito.getAnoBase() + n,
                toneladas,
                precoUsd,
                precoReais,
                receitaUsd,
                receitaReais));
        }
        return itens;
    }

    /** Constrói os dados de gráfico a partir da série anual projetada. */
    static GraficoProjecaoDTO construirGrafico(List<ProjecaoAnualDTO> itens) {
        List<Integer> labels = itens.stream().map(ProjecaoAnualDTO::ano).toList();
        List<BigDecimal> receitaReaisSerie = itens.stream()
            .map(ProjecaoAnualDTO::receitaProjetadaReais).toList();
        List<BigDecimal> receitaUsdSerie = itens.stream()
            .map(ProjecaoAnualDTO::receitaProjetadaUsd).toList();
        List<BigDecimal> tco2eSerie = itens.stream()
            .map(ProjecaoAnualDTO::toneladasProjetadas).toList();
        List<BigDecimal> precoUsdSerie = itens.stream()
            .map(ProjecaoAnualDTO::precoToneladaAnoUsd).toList();
        List<BigDecimal> precoReaisSerie = itens.stream()
            .map(ProjecaoAnualDTO::precoToneladaAnoReais).toList();
        return new GraficoProjecaoDTO(
            labels,
            new SerieDTO("Receita Projetada (R$)", receitaReaisSerie),
            new SerieDTO("Receita Projetada (USD)", receitaUsdSerie),
            new SerieDTO("tCO₂e Projetadas", tco2eSerie),
            new SerieDTO("Preço/t (USD)", precoUsdSerie),
            new SerieDTO("Preço/t (R$)", precoReaisSerie)
        );
    }

    private BigDecimal somarTco2e(List<ProjecaoAnualDTO> itens) {
        return itens.stream().map(ProjecaoAnualDTO::toneladasProjetadas)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal somarUsd(List<ProjecaoAnualDTO> itens) {
        return itens.stream().map(ProjecaoAnualDTO::receitaProjetadaUsd)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal somarReais(List<ProjecaoAnualDTO> itens) {
        return itens.stream().map(ProjecaoAnualDTO::receitaProjetadaReais)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
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
