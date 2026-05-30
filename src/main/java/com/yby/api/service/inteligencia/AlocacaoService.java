package com.yby.api.service.inteligencia;

import com.yby.api.dto.AlocacaoItemDTO;
import com.yby.api.dto.AlocacaoRequestDTO;
import com.yby.api.dto.AlocacaoResponseDTO;
import com.yby.api.entity.Indicador;
import com.yby.api.entity.Municipio;
import com.yby.api.entity.enums.EstrategiaAlocacao;
import com.yby.api.exception.BusinessException;
import com.yby.api.repository.IndicadorRepository;
import com.yby.api.repository.MunicipioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RN-200 - Otimizacao de Alocacao de orcamento publico.
 *
 * <p>Sugere a distribuicao de um orcamento entre municipios segundo a estrategia escolhida,
 * respeitando restricoes de numero de contemplados e piso por municipio. Para cada municipio
 * retorna valor alocado, retorno ambiental esperado, impacto social e justificativa auditavel
 * (transparencia - RN-300; eficiencia do gasto - CF art. 37).</p>
 */
@Service
public class AlocacaoService {

    private static final int DEFAULT_MAX_MUNICIPIOS = 20;

    private final MunicipioRepository municipioRepository;
    private final IndicadorRepository indicadorRepository;

    public AlocacaoService(MunicipioRepository municipioRepository, IndicadorRepository indicadorRepository) {
        this.municipioRepository = municipioRepository;
        this.indicadorRepository = indicadorRepository;
    }

    @Transactional(readOnly = true)
    public AlocacaoResponseDTO otimizar(AlocacaoRequestDTO request) {
        BigDecimal orcamento = request.orcamentoTotal();
        EstrategiaAlocacao estrategia = request.estrategia();

        List<Candidato> candidatos = carregarCandidatos();
        if (candidatos.isEmpty()) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Nao ha municipios com score para alocar orcamento");
        }

        BigDecimal maxKpi = candidatos.stream().map(Candidato::kpi).reduce(BigDecimal.ZERO, BigDecimal::max);
        candidatos.forEach(c -> c.setPeso(peso(estrategia, c, maxKpi)));
        candidatos.sort(Comparator.comparing(Candidato::peso).reversed());

        int n = limiteContemplados(request, orcamento, candidatos.size());
        List<Candidato> selecionados = candidatos.subList(0, n);

        List<AlocacaoItemDTO> itens = distribuir(orcamento, estrategia, selecionados);

        BigDecimal totalAlocado = itens.stream()
            .map(AlocacaoItemDTO::valorAlocado).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal retornoTotal = itens.stream()
            .map(AlocacaoItemDTO::retornoEsperado).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new AlocacaoResponseDTO(orcamento, request.ano(), estrategia,
            totalAlocado, retornoTotal.setScale(2, RoundingMode.HALF_UP), itens, AlgoritmoMetadata.VERSAO);
    }

    private List<Candidato> carregarCandidatos() {
        List<Candidato> candidatos = new ArrayList<>();
        for (Municipio m : municipioRepository.findAll()) {
            if (m.getScorePrioridade() == null) {
                continue;
            }
            Indicador ultimo = indicadorRepository.findTopByMunicipioIdOrderByAnoDesc(m.getId()).orElse(null);
            candidatos.add(new Candidato(m, kpiRetorno(ultimo), impactoSocial(ultimo)));
        }
        return candidatos;
    }

    private int limiteContemplados(AlocacaoRequestDTO request, BigDecimal orcamento, int totalCandidatos) {
        int limite = (request.maxMunicipios() != null && request.maxMunicipios() > 0)
            ? Math.min(request.maxMunicipios(), totalCandidatos)
            : Math.min(DEFAULT_MAX_MUNICIPIOS, totalCandidatos);

        if (request.valorMinimoPorMunicipio() != null
            && request.valorMinimoPorMunicipio().compareTo(BigDecimal.ZERO) > 0) {
            int porPiso = orcamento.divide(request.valorMinimoPorMunicipio(), 0, RoundingMode.DOWN).intValue();
            if (porPiso <= 0) {
                throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Orcamento insuficiente para o valor minimo por municipio");
            }
            limite = Math.min(limite, porPiso);
        }
        return Math.max(limite, 1);
    }

    private List<AlocacaoItemDTO> distribuir(BigDecimal orcamento, EstrategiaAlocacao estrategia,
                                             List<Candidato> selecionados) {
        BigDecimal somaPesos = selecionados.stream().map(Candidato::peso).reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean igualitario = estrategia == EstrategiaAlocacao.EQUIDADE_REGIONAL
            || somaPesos.compareTo(BigDecimal.ZERO) <= 0;

        List<AlocacaoItemDTO> itens = new ArrayList<>();
        BigDecimal acumulado = BigDecimal.ZERO;
        int total = selecionados.size();

        for (int i = 0; i < total; i++) {
            Candidato c = selecionados.get(i);
            BigDecimal valor;
            if (i == total - 1) {
                valor = orcamento.subtract(acumulado); // ultimo absorve arredondamento -> soma exata
            } else if (igualitario) {
                valor = orcamento.divide(BigDecimal.valueOf(total), 2, RoundingMode.DOWN);
            } else {
                valor = orcamento.multiply(c.peso())
                    .divide(somaPesos, 2, RoundingMode.DOWN);
            }
            valor = valor.max(BigDecimal.ZERO);
            acumulado = acumulado.add(valor);

            BigDecimal retorno = valor.multiply(c.kpi()).setScale(2, RoundingMode.HALF_UP);
            itens.add(new AlocacaoItemDTO(c.municipio().getId(), c.municipio().getNome(),
                c.municipio().getScorePrioridade(), valor.setScale(2, RoundingMode.HALF_UP), retorno,
                c.impactoSocial(), justificativa(estrategia, c)));
        }
        return itens;
    }

    private BigDecimal peso(EstrategiaAlocacao estrategia, Candidato c, BigDecimal maxKpi) {
        BigDecimal score = c.municipio().getScorePrioridade();
        return switch (estrategia) {
            case EQUIDADE_REGIONAL -> BigDecimal.ONE;
            case MAXIMO_KPI -> {
                BigDecimal kpiNorm = maxKpi.compareTo(BigDecimal.ZERO) > 0
                    ? c.kpi().divide(maxKpi, 6, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
                yield score.multiply(BigDecimal.ONE.add(kpiNorm));
            }
            case RISCO_MINIMO -> {
                BigDecimal risco = c.municipio().getNotaRisco() == null ? BigDecimal.ZERO : c.municipio().getNotaRisco();
                BigDecimal fatorRisco = BigDecimal.ONE
                    .subtract(risco.divide(BigDecimal.TEN, 6, RoundingMode.HALF_UP)).max(BigDecimal.ZERO);
                yield score.multiply(fatorRisco);
            }
        };
    }

    private String justificativa(EstrategiaAlocacao estrategia, Candidato c) {
        BigDecimal score = c.municipio().getScorePrioridade();
        return switch (estrategia) {
            case MAXIMO_KPI -> "Estrategia MAXIMO_KPI: score " + score
                + ", KPI de retorno " + c.kpi() + " (resultado ambiental por R$).";
            case EQUIDADE_REGIONAL -> "Estrategia EQUIDADE_REGIONAL: distribuicao igualitaria entre os "
                + "contemplados para desconcentrar o investimento (score " + score + ").";
            case RISCO_MINIMO -> "Estrategia RISCO_MINIMO: prioridade " + score
                + " ajustada pela nota de risco " + c.municipio().getNotaRisco() + ".";
        };
    }

    private BigDecimal kpiRetorno(Indicador indicador) {
        if (indicador == null || indicador.getGastoPublico() == null
            || indicador.getGastoPublico().compareTo(BigDecimal.ZERO) <= 0
            || indicador.getResultadoAmbiental() == null) {
            return BigDecimal.ZERO;
        }
        return indicador.getResultadoAmbiental().divide(indicador.getGastoPublico(), 6, RoundingMode.HALF_UP);
    }

    private BigDecimal impactoSocial(Indicador indicador) {
        if (indicador == null) {
            return BigDecimal.ZERO;
        }
        int empregos = indicador.getEmpregosConservacao() == null ? 0 : indicador.getEmpregosConservacao();
        int familias = indicador.getFamiliasPsa() == null ? 0 : indicador.getFamiliasPsa();
        return BigDecimal.valueOf(empregos + familias);
    }

    private static final class Candidato {
        private final Municipio municipio;
        private final BigDecimal kpi;
        private final BigDecimal impactoSocial;
        private BigDecimal peso = BigDecimal.ZERO;

        Candidato(Municipio municipio, BigDecimal kpi, BigDecimal impactoSocial) {
            this.municipio = municipio;
            this.kpi = kpi;
            this.impactoSocial = impactoSocial;
        }

        Municipio municipio() {
            return municipio;
        }

        BigDecimal kpi() {
            return kpi;
        }

        BigDecimal impactoSocial() {
            return impactoSocial;
        }

        BigDecimal peso() {
            return peso;
        }

        void setPeso(BigDecimal peso) {
            this.peso = peso;
        }
    }
}
