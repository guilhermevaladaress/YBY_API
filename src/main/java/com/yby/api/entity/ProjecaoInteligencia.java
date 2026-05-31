package com.yby.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Snapshot do resultado preditivo de inteligencia por municipio (RN-101-A .. RN-300).
 *
 * <p>Persistido pelo {@code RecalculoInteligenciaService}: combina a serie historica de
 * desmatamento (PRODES) com os focos de calor do INPE e armazena tendencia, projecao,
 * risco e carbono evitado. Um snapshot por municipio (o mais recente).</p>
 */
@Getter
@Setter
@Entity
@Table(name = "projecoes_inteligencia")
public class ProjecaoInteligencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "municipio_id", nullable = false, unique = true)
    private Municipio municipio;

    @Column(name = "ano_base")
    private Integer anoBase;

    @Column(name = "tendencia_percent", precision = 8, scale = 2)
    private BigDecimal tendenciaPercent;

    @Column(name = "status_tendencia", length = 20)
    private String statusTendencia;

    @Column(name = "ajuste_score")
    private Integer ajusteScore;

    @Column(name = "taxa_media_anual", precision = 8, scale = 2)
    private BigDecimal taxaMediaAnual;

    @Column(name = "media_historica_ha", precision = 18, scale = 2)
    private BigDecimal mediaHistoricaHa;

    @Column(name = "horizonte_anos")
    private Integer horizonteAnos;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "projecao_json", columnDefinition = "jsonb")
    private String projecaoJson;

    @Column(name = "nota_risco", precision = 4, scale = 2)
    private BigDecimal notaRisco;

    @Column(name = "semaforo_risco", length = 20)
    private String semaforoRisco;

    @Column(name = "score_projetado", precision = 5, scale = 2)
    private BigDecimal scoreProjetado;

    @Column(name = "tco2e_evitado", precision = 18, scale = 4)
    private BigDecimal tco2eEvitado;

    @Column(name = "valor_potencial_reais", precision = 18, scale = 2)
    private BigDecimal valorPotencialReais;

    @Column(name = "focos_calor_ano")
    private Integer focosCalorAno;

    @Column(name = "versao_algoritmo", nullable = false, length = 40)
    private String versaoAlgoritmo;

    @Column(name = "calculado_em", nullable = false)
    private OffsetDateTime calculadoEm;
}
