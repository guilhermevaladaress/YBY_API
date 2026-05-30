package com.yby.api.entity;

import com.yby.api.entity.enums.Bioma;
import com.yby.api.entity.enums.Semaforo;
import com.yby.api.entity.enums.VulnerabilidadeHidrica;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "municipios")
public class Municipio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "codigo_ibge", nullable = false, unique = true, length = 7)
    private String codigoIbge;

    @Column(name = "score_prioridade", precision = 5, scale = 2)
    private BigDecimal scorePrioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Semaforo semaforo = Semaforo.AMARELO;

    @Column(name = "area_ha", precision = 18, scale = 2)
    private BigDecimal areaHa;

    @Column(name = "geojson_polygon", columnDefinition = "jsonb")
    private String geojsonPolygon;

    @Column(name = "nota_risco", precision = 4, scale = 2)
    private BigDecimal notaRisco;

    @Column(name = "kpi_retorno", precision = 18, scale = 6)
    private BigDecimal kpiRetorno;

    @Column(name = "gasto_publico", precision = 18, scale = 2)
    private BigDecimal gastoPublico;

    @Column(name = "resultado_ambiental", precision = 18, scale = 2)
    private BigDecimal resultadoAmbiental;

    @Column(name = "pendencias_resumo", length = 2000)
    private String pendenciasResumo;

    /** Bioma principal do municipio (RN-106-A). Cerrado e o predominante no Tocantins. */
    @Enumerated(EnumType.STRING)
    @Column(name = "bioma")
    private Bioma bioma = Bioma.CERRADO;

    /** Vulnerabilidade hidrica usada para calibrar o semaforo em biomas sensiveis (RN-106-A). */
    @Enumerated(EnumType.STRING)
    @Column(name = "vulnerabilidade_hidrica")
    private VulnerabilidadeHidrica vulnerabilidadeHidrica = VulnerabilidadeHidrica.BAIXA;

    /** Indica plano de acao ambiental ativo; reduz o risco preditivo (RN-108-A, fator x0,9). */
    @Column(name = "plano_acao_ativo", nullable = false)
    private boolean planoAcaoAtivo = false;

    @Column(name = "ultima_atualizacao")
    private OffsetDateTime ultimaAtualizacao;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.ultimaAtualizacao == null) {
            this.ultimaAtualizacao = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.updatedAt = now;
        this.ultimaAtualizacao = now;
    }
}
