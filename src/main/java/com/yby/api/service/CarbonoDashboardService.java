package com.yby.api.service;

import com.yby.api.dto.CarbonoDashboardDTO;
import com.yby.api.dto.CarbonoDashboardDTO.CompradorReferenciaDTO;
import com.yby.api.dto.CarbonoDashboardDTO.MunicipioReceitaDTO;
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
 * <p>Agrega a projecao financeira dos creditos, a distribuicao por status, o potencial de
 * financiamento do Plano Safra (linhas de baixa emissao) e o mercado comprador cadastrado.</p>
 */
@Service
public class CarbonoDashboardService {

    private final CreditoCarbonoService creditoCarbonoService;
    private final CreditoCarbonoRepository creditoCarbonoRepository;
    private final ProjetoJreddRepository projetoJreddRepository;
    private final InstituicaoCarbonoRepository instituicaoCarbonoRepository;
    private final LinhaCreditoSafraRepository linhaCreditoSafraRepository;

    public CarbonoDashboardService(CreditoCarbonoService creditoCarbonoService,
                                   CreditoCarbonoRepository creditoCarbonoRepository,
                                   ProjetoJreddRepository projetoJreddRepository,
                                   InstituicaoCarbonoRepository instituicaoCarbonoRepository,
                                   LinhaCreditoSafraRepository linhaCreditoSafraRepository) {
        this.creditoCarbonoService = creditoCarbonoService;
        this.creditoCarbonoRepository = creditoCarbonoRepository;
        this.projetoJreddRepository = projetoJreddRepository;
        this.instituicaoCarbonoRepository = instituicaoCarbonoRepository;
        this.linhaCreditoSafraRepository = linhaCreditoSafraRepository;
    }

    public CarbonoDashboardDTO consolidar(Integer ano) {
        List<CreditoCarbono> creditos = creditoCarbonoRepository.findAll();

        ProjecaoCarbonoDTO consolidado = creditoCarbonoService.projetarConsolidado(ano, null);

        Map<String, Long> creditosPorStatus = creditos.stream()
            .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));

        List<MunicipioReceitaDTO> topMunicipios = creditos.stream()
            .map(c -> c.getMunicipio().getId())
            .distinct()
            .map(municipioId -> {
                ProjecaoCarbonoDTO p = creditoCarbonoService.projetarConsolidado(ano, municipioId);
                return new MunicipioReceitaDTO(municipioId, p.tco2eTotal(), p.receitaTotalReais());
            })
            .sorted(Comparator.comparing(MunicipioReceitaDTO::receitaTotalReais,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed())
            .limit(10)
            .toList();

        List<InstituicaoCarbono> instituicoes = instituicaoCarbonoRepository.findAll();

        List<CompradorReferenciaDTO> melhoresCompradores = instituicoes.stream()
            .filter(InstituicaoCarbono::isAtivo)
            .filter(i -> i.getPrecoReferenciaTonelada() != null)
            .sorted(Comparator.comparing(InstituicaoCarbono::getPrecoReferenciaTonelada).reversed())
            .limit(5)
            .map(i -> new CompradorReferenciaDTO(i.getId(), i.getNome(), i.getTipo().name(),
                i.getPrecoReferenciaTonelada(), i.getMoeda()))
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
            consolidado.receitaTotalReais(),
            ordenar(creditosPorStatus),
            consolidado.itens(),
            topMunicipios,
            melhoresCompradores,
            potencialSafra,
            linhasCarbono.size(),
            instituicoesPorTipo,
            AlgoritmoMetadata.VERSAO
        );
    }

    private Map<String, Long> ordenar(Map<String, Long> mapa) {
        return mapa.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (a, b) -> a, LinkedHashMap::new));
    }
}
