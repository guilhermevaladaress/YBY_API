package com.yby.api.service.inteligencia;

import com.yby.api.dto.DesperdicioInteligenteDTO;
import com.yby.api.entity.Indicador;
import com.yby.api.entity.Municipio;
import com.yby.api.entity.enums.Bioma;
import com.yby.api.entity.enums.Porte;
import com.yby.api.repository.IndicadorRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RN-107-A - Alerta de Desperdicio Inteligente (benchmarking).
 *
 * <p>Detecta ineficiencia relativa comparando municipios semelhantes (mesmo bioma e porte).
 * O custo por unidade de resultado ambiental (gasto/resultado) e confrontado com a media do
 * grupo: acima de 2x a media => alerta vermelho; entre 1x e 2x => amarelo.</p>
 */
@Service
public class DesperdicioInteligenteService {

    private static final BigDecimal LIMIAR_VERMELHO = new BigDecimal("2");

    private final IndicadorRepository indicadorRepository;

    public DesperdicioInteligenteService(IndicadorRepository indicadorRepository) {
        this.indicadorRepository = indicadorRepository;
    }

    @Transactional(readOnly = true)
    public List<DesperdicioInteligenteDTO> avaliar(Integer ano) {
        int anoRef = ano == null ? Year.now().getValue() : ano;

        List<Avaliacao> base = new ArrayList<>();
        for (Indicador indicador : indicadorRepository.findByAno(anoRef)) {
            BigDecimal custo = custoPorResultado(indicador.getGastoPublico(), indicador.getResultadoAmbiental());
            if (custo == null) {
                continue; // sem dados suficientes para avaliar eficiencia
            }
            Municipio m = indicador.getMunicipio();
            Bioma bioma = m.getBioma() == null ? Bioma.CERRADO : m.getBioma();
            Porte porte = Porte.deArea(m.getAreaHa());
            base.add(new Avaliacao(m, bioma, porte, indicador.getGastoPublico(),
                indicador.getResultadoAmbiental(), custo));
        }

        Map<String, BigDecimal> mediaPorGrupo = mediasPorGrupo(base);

        List<DesperdicioInteligenteDTO> resultado = new ArrayList<>();
        for (Avaliacao a : base) {
            BigDecimal media = mediaPorGrupo.get(chave(a.bioma(), a.porte()));
            BigDecimal razao = (media == null || media.compareTo(BigDecimal.ZERO) <= 0)
                ? BigDecimal.ZERO
                : a.custo().divide(media, 4, RoundingMode.HALF_UP);
            resultado.add(new DesperdicioInteligenteDTO(
                a.municipio().getId(), a.municipio().getNome(), a.bioma(), a.porte(),
                a.gasto(), a.resultado(), a.custo(),
                media == null ? BigDecimal.ZERO : media.setScale(4, RoundingMode.HALF_UP),
                razao, nivelAlerta(razao), AlgoritmoMetadata.VERSAO));
        }

        resultado.sort((x, y) -> y.razaoSobreMedia().compareTo(x.razaoSobreMedia()));
        return resultado;
    }

    private Map<String, BigDecimal> mediasPorGrupo(List<Avaliacao> base) {
        Map<String, List<BigDecimal>> agrupado = new LinkedHashMap<>();
        for (Avaliacao a : base) {
            agrupado.computeIfAbsent(chave(a.bioma(), a.porte()), k -> new ArrayList<>()).add(a.custo());
        }
        Map<String, BigDecimal> medias = new LinkedHashMap<>();
        agrupado.forEach((grupo, custos) -> {
            BigDecimal soma = custos.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            medias.put(grupo, soma.divide(BigDecimal.valueOf(custos.size()), 6, RoundingMode.HALF_UP));
        });
        return medias;
    }

    private String nivelAlerta(BigDecimal razao) {
        if (razao.compareTo(LIMIAR_VERMELHO) > 0) {
            return "vermelho";
        }
        if (razao.compareTo(BigDecimal.ONE) > 0) {
            return "amarelo";
        }
        return "verde";
    }

    /** gasto / resultado; null quando indeterminavel (RN-104: nunca divide por zero). */
    BigDecimal custoPorResultado(BigDecimal gasto, BigDecimal resultado) {
        if (gasto == null || gasto.compareTo(BigDecimal.ZERO) <= 0
            || resultado == null || resultado.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        return gasto.divide(resultado, 6, RoundingMode.HALF_UP);
    }

    private String chave(Bioma bioma, Porte porte) {
        return bioma.name() + "|" + porte.name();
    }

    private record Avaliacao(Municipio municipio, Bioma bioma, Porte porte,
                             BigDecimal gasto, BigDecimal resultado, BigDecimal custo) {
    }
}
