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
