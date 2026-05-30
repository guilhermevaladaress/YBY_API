package com.yby.api.service;

import com.yby.api.dto.AreaCarbonoEstudoDTO;
import com.yby.api.dto.CamadaGeoportalDTO;
import com.yby.api.entity.Municipio;
import com.yby.api.entity.enums.Bioma;
import com.yby.api.integration.seplan.SeplanFeatureCollectionDTO;
import com.yby.api.integration.seplan.SeplanGeoportalClient;
import com.yby.api.repository.MunicipioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Integracao com o Geoportal da SEPLAN-TO para apoio ao estudo de areas de carbono.
 *
 * <p>Combina um catalogo curado das camadas territoriais relevantes (sempre disponivel) com a
 * consulta direta ao GeoServer da SEPLAN e com a sugestao inteligente de areas prioritarias,
 * cruzando os dados locais dos municipios (bioma, area, score) com o potencial de carbono.</p>
 */
@Service
public class GeoportalService {

    private final SeplanGeoportalClient client;
    private final MunicipioRepository municipioRepository;

    public GeoportalService(SeplanGeoportalClient client, MunicipioRepository municipioRepository) {
        this.client = client;
        this.municipioRepository = municipioRepository;
    }

    /** Catalogo curado das camadas do Geoportal SEPLAN relevantes para estudo de carbono. */
    public List<CamadaGeoportalDTO> catalogoCamadas() {
        return List.of(
            new CamadaGeoportalDTO("cobertura_vegetal", "Cobertura Vegetal e Uso do Solo",
                "COBERTURA_VEGETAL", "seplan:cobertura_vegetal_uso_solo",
                "Base para estimar estoque de carbono da vegetacao nativa e areas convertidas; "
                    + "essencial para a linha de base de projetos REDD+/carbono.", "ALTA"),
            new CamadaGeoportalDTO("unidades_conservacao", "Unidades de Conservacao",
                "UNIDADE_CONSERVACAO", "seplan:unidades_conservacao",
                "Areas protegidas elegiveis a projetos de conservacao e pagamento por servicos ambientais.",
                "ALTA"),
            new CamadaGeoportalDTO("zee", "Zoneamento Ecologico-Economico (ZEE-TO)",
                "ZONEAMENTO", "seplan:zee_to",
                "Define vocacao e restricoes de uso do territorio, orientando onde projetos de carbono "
                    + "sao viaveis e prioritarios.", "ALTA"),
            new CamadaGeoportalDTO("bacias_hidrograficas", "Bacias Hidrograficas",
                "HIDROGRAFIA", "seplan:bacias_hidrograficas",
                "Apoia cobeneficios hidricos e a delimitacao de APPs em projetos de restauracao.", "MEDIA"),
            new CamadaGeoportalDTO("assentamentos", "Assentamentos Rurais",
                "FUNDIARIO", "seplan:assentamentos_rurais",
                "Identifica agricultura familiar elegivel ao Pronaf Bioeconomia e a creditos comunitarios.",
                "MEDIA"),
            new CamadaGeoportalDTO("municipios", "Limites Municipais",
                "LIMITE_ADMINISTRATIVO", "seplan:limite_municipal",
                "Recorte jurisdicional para consolidar metas de carbono por municipio (JREDD+).", "BAIXA")
        );
    }

    /**
     * Consulta direta a uma camada do Geoportal SEPLAN (WFS).
     * Resiliente: retorna colecao vazia/{@code disponivel=false} quando a fonte esta indisponivel.
     */
    public SeplanFeatureCollectionDTO consultarCamada(String typeName, String cqlFilter, int maxFeatures) {
        return client.buscarCamada(typeName, cqlFilter, Math.min(Math.max(maxFeatures, 1), 1000));
    }

    /**
     * Sugere areas prioritarias para estudo de carbono, do maior score para o menor,
     * com o potencial estimado de tCO2e/ano e as camadas do geoportal recomendadas.
     */
    public List<AreaCarbonoEstudoDTO> sugerirAreas(int limite) {
        List<CamadaGeoportalDTO> camadasAlta = catalogoCamadas().stream()
            .filter(c -> "ALTA".equals(c.relevanciaCarbono()))
            .toList();

        return municipioRepository
            .findAll(PageRequest.of(0, Math.min(Math.max(limite, 1), 50),
                Sort.by(Sort.Direction.DESC, "scorePrioridade")))
            .getContent().stream()
            .map(m -> toAreaEstudo(m, camadasAlta))
            .toList();
    }

    private AreaCarbonoEstudoDTO toAreaEstudo(Municipio m, List<CamadaGeoportalDTO> camadas) {
        BigDecimal potencial = potencialTco2eAno(m);
        String justificativa = montarJustificativa(m);
        return new AreaCarbonoEstudoDTO(
            m.getId(), m.getNome(), m.getCodigoIbge(),
            m.getBioma() == null ? null : m.getBioma().name(),
            m.getAreaHa(), m.getScorePrioridade(),
            m.getSemaforo() == null ? null : m.getSemaforo().name(),
            potencial, justificativa, camadas);
    }

    /**
     * Potencial anual de carbono estimado pela area e fator do bioma (heuristica de triagem;
     * o valor definitivo depende de inventario de campo e metodologia certificada).
     */
    private BigDecimal potencialTco2eAno(Municipio m) {
        if (m.getAreaHa() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal fator = fatorBioma(m.getBioma());
        return m.getAreaHa().multiply(fator).setScale(2, RoundingMode.HALF_UP);
    }

    /** Fator ilustrativo de sequestro/evitamento por hectare ao ano, por bioma (tCO2e/ha). */
    private BigDecimal fatorBioma(Bioma bioma) {
        if (bioma == Bioma.AMAZONIA) {
            return new BigDecimal("0.0450");
        }
        if (bioma == Bioma.CERRADO) {
            return new BigDecimal("0.0180");
        }
        return new BigDecimal("0.0100");
    }

    private String montarJustificativa(Municipio m) {
        StringBuilder sb = new StringBuilder("Area sugerida para estudo de carbono");
        if (m.getBioma() != null) {
            sb.append(" no bioma ").append(m.getBioma().name());
        }
        if (m.getSemaforo() != null) {
            sb.append(", situacao ").append(m.getSemaforo().name());
        }
        if (m.getScorePrioridade() != null) {
            sb.append(", score de prioridade ").append(m.getScorePrioridade());
        }
        sb.append(". Cruzar com as camadas de cobertura vegetal, unidades de conservacao e ZEE "
            + "do Geoportal SEPLAN para delimitar a linha de base.");
        return sb.toString();
    }
}
