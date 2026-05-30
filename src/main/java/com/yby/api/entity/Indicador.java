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

    @Column(name = "gasto_publico", precision = 14, scale = 2)
    private BigDecimal gastoPublico;

    @Column(name = "resultado_ambiental", precision = 14, scale = 2)
    private BigDecimal resultadoAmbiental;

    @Column(name = "eficiencia_gasto", precision = 6, scale = 2)
    private BigDecimal eficienciaGasto;

    @Column(name = "irregularidades_car")
    private Integer irregularidadesCar;

    @Column(name = "area_elegivel_ha", precision = 14, scale = 2)
    private BigDecimal areaElegivelHa;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        var now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
