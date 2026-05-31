package com.yby.api.service;

import com.yby.api.dto.CarbonoDashboardDTO;
import com.yby.api.dto.CarbonoDashboardDTO.CompradorReferenciaDTO;
import com.yby.api.dto.CarbonoDashboardDTO.MunicipioReceitaDTO;
import com.yby.api.dto.CotacaoDolarDTO;
import com.yby.api.dto.InfoCreditoCarbonoDTO;
import com.yby.api.dto.ProjecaoCarbonoDTO;
import com.yby.api.entity.CreditoCarbono;
import com.yby.api.entity.InstituicaoCarbono;
import com.yby.api.entity.LinhaCreditoSafra;
import com.yby.api.repository.CreditoCarbonoRepository;
import com.yby.api.repository.InstituicaoCarbonoRepository;
import com.yby.api.repository.LinhaCreditoSafraRepository;
import com.yby.api.repository.ProjetoJreddRepository;
import com.yby.api.service.inteligencia.AlgoritmoMetadata;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Consolida os indicadores de carbono em um painel executivo para o dashboard profissional.
 *
 * <p>Busca a cotação USD/BRL uma única vez por requisição e a repassa para todos os
 * cálculos de projeção, garantindo consistência entre série anual, top municípios e
 * melhores compradores.</p>
 */
@Service
public class CarbonoDashboardService {

    private final CreditoCarbonoService creditoCarbonoService;
    private final CreditoCarbonoRepository creditoCarbonoRepository;
    private final ProjetoJreddRepository projetoJreddRepository;
    private final InstituicaoCarbonoRepository instituicaoCarbonoRepository;
    private final LinhaCreditoSafraRepository linhaCreditoSafraRepository;
    private final CotacaoDolarService cotacaoDolarService;

    public CarbonoDashboardService(CreditoCarbonoService creditoCarbonoService,
                                   CreditoCarbonoRepository creditoCarbonoRepository,
                                   ProjetoJreddRepository projetoJreddRepository,
                                   InstituicaoCarbonoRepository instituicaoCarbonoRepository,
                                   LinhaCreditoSafraRepository linhaCreditoSafraRepository,
                                   CotacaoDolarService cotacaoDolarService) {
        this.creditoCarbonoService = creditoCarbonoService;
        this.creditoCarbonoRepository = creditoCarbonoRepository;
        this.projetoJreddRepository = projetoJreddRepository;
        this.instituicaoCarbonoRepository = instituicaoCarbonoRepository;
        this.linhaCreditoSafraRepository = linhaCreditoSafraRepository;
        this.cotacaoDolarService = cotacaoDolarService;
    }

    /**
     * Gera o painel executivo consolidado de carbono.
     *
     * @param ano                   filtra a série anual para anos &gt;= ano; nulo = todos
     * @param dataReferenciaCotacao data para busca da cotação USD/BRL; nulo = hoje
     */
    public CarbonoDashboardDTO consolidar(Integer ano, LocalDate dataReferenciaCotacao) {
        // Cotação buscada uma única vez para toda a requisição
        CotacaoDolarDTO cotacao = cotacaoDolarService.buscar(dataReferenciaCotacao);

        List<CreditoCarbono> creditos = creditoCarbonoRepository.findAll();

        ProjecaoCarbonoDTO consolidado =
            creditoCarbonoService.projetarConsolidadoComCotacao(ano, null, cotacao);

        Map<String, Long> creditosPorStatus = creditos.stream()
            .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));

        List<MunicipioReceitaDTO> topMunicipios = creditos.stream()
            .map(c -> c.getMunicipio().getId())
            .distinct()
            .map(municipioId -> {
                ProjecaoCarbonoDTO p =
                    creditoCarbonoService.projetarConsolidadoComCotacao(ano, municipioId, cotacao);
                return new MunicipioReceitaDTO(
                    municipioId, p.tco2eTotal(), p.receitaTotalUsd(), p.receitaTotalReais());
            })
            .sorted(Comparator.comparing(MunicipioReceitaDTO::receitaTotalReais,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .limit(10)
            .toList();

        List<InstituicaoCarbono> instituicoes = instituicaoCarbonoRepository.findAll();

        List<CompradorReferenciaDTO> melhoresCompradores = instituicoes.stream()
            .filter(InstituicaoCarbono::isAtivo)
            .filter(i -> i.getPrecoReferenciaTonelada() != null)
            .sorted(Comparator.comparing(i -> precoEmUsd(i, cotacao.taxaUsdBrl()),
                Comparator.reverseOrder()))
            .limit(5)
            .map(i -> new CompradorReferenciaDTO(
                i.getId(), i.getNome(), i.getTipo().name(),
                i.getPrecoReferenciaTonelada(),
                converterParaBrl(i, cotacao.taxaUsdBrl()),
                i.getMoeda()))
            .toList();

        Map<String, Long> instituicoesPorTipo = instituicoes.stream()
            .collect(Collectors.groupingBy(i -> i.getTipo().name(), TreeMap::new, Collectors.counting()));

        List<LinhaCreditoSafra> linhasCarbono = linhaCreditoSafraRepository
            .findByExigePraticaCarbonoTrueAndAtivoTrue();
        BigDecimal potencialSafra = linhasCarbono.stream()
            .map(LinhaCreditoSafra::getTetoFinanciamento)
            .filter(t -> t != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CarbonoDashboardDTO(
            OffsetDateTime.now(),
            creditos.size(),
            projetoJreddRepository.count(),
            instituicoes.size(),
            consolidado.tco2eTotal(),
            consolidado.receitaTotalUsd(),
            consolidado.receitaTotalReais(),
            cotacao,
            ordenar(creditosPorStatus),
            consolidado.itens(),
            consolidado.grafico(),
            topMunicipios,
            melhoresCompradores,
            potencialSafra,
            linhasCarbono.size(),
            instituicoesPorTipo,
            InfoCreditoCarbonoDTO.padrao(),
            AlgoritmoMetadata.VERSAO
        );
    }

    /** Normaliza o preço de uma instituição para USD para fins de ordenação. */
    private BigDecimal precoEmUsd(InstituicaoCarbono i, BigDecimal taxaUsdBrl) {
        if (i.getPrecoReferenciaTonelada() == null) {
            return BigDecimal.ZERO;
        }
        if ("BRL".equalsIgnoreCase(i.getMoeda()) && taxaUsdBrl.compareTo(BigDecimal.ZERO) > 0) {
            return i.getPrecoReferenciaTonelada()
                .divide(taxaUsdBrl, 4, RoundingMode.HALF_UP);
        }
        return i.getPrecoReferenciaTonelada();
    }

    /** Converte o preço de uma instituição para BRL se estiver em USD. */
    private BigDecimal converterParaBrl(InstituicaoCarbono i, BigDecimal taxaUsdBrl) {
        if (i.getPrecoReferenciaTonelada() == null) {
            return null;
        }
        if ("USD".equalsIgnoreCase(i.getMoeda())) {
            return i.getPrecoReferenciaTonelada()
                .multiply(taxaUsdBrl).setScale(2, RoundingMode.HALF_UP);
        }
        return i.getPrecoReferenciaTonelada().setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Long> ordenar(Map<String, Long> mapa) {
        return mapa.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (a, b) -> a, LinkedHashMap::new));
    }
}
