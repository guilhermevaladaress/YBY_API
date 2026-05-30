package com.yby.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "indicadores",
    uniqueConstraints = @UniqueConstraint(name = "uk_indicadores_municipio_ano", columnNames = {"municipio_id", "ano"})
)
public class Indicador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "municipio_id", nullable = false)
    private Municipio municipio;

    @Column(nullable = false)
    private Integer ano;

    @Column(name = "gasto_publico", precision = 18, scale = 2)
    private BigDecimal gastoPublico;

    @Column(name = "resultado_ambiental", precision = 18, scale = 2)
    private BigDecimal resultadoAmbiental;

    @Column(name = "desmatamento_recente_factor", precision = 5, scale = 2)
    private BigDecimal desmatamentoRecenteFactor;

    @Column(name = "eficiencia_gasto_factor", precision = 5, scale = 2)
    private BigDecimal eficienciaGastoFactor;

    @Column(name = "irregularidades_car_factor", precision = 5, scale = 2)
    private BigDecimal irregularidadesCarFactor;

    @Column(name = "area_elegivel_factor", precision = 5, scale = 2)
    private BigDecimal areaElegivelFactor;

    // --- Indicadores sociais (RN-103-B: KPI multidimensional) ---

    /** Empregos formais em conservacao/restauracao gerados no ano. */
    @Column(name = "empregos_conservacao")
    private Integer empregosConservacao;

    /** Familias beneficiadas por Pagamento por Servicos Ambientais (Lei 14.119/2021). */
    @Column(name = "familias_psa")
    private Integer familiasPsa;

    /** Comunidades tradicionais (quilombolas, indigenas, extrativistas) envolvidas. */
    @Column(name = "comunidades_tradicionais")
    private Integer comunidadesTradicionais;

    /** Investimento social aplicado no ano (R$); habilita o bonus de 5-15% da RN-103-B. */
    @Column(name = "investimento_social", precision = 18, scale = 2)
    private BigDecimal investimentoSocial;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
