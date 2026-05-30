package com.yby.api.entity;

import com.yby.api.entity.enums.Semaforo;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "municipios")
public class Municipio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "codigo_ibge", nullable = false, unique = true, length = 7)
    private String codigoIbge;

    @Column(name = "area_ha", precision = 14, scale = 2)
    private BigDecimal areaHa;

    @Column(name = "score_prioridade", precision = 5, scale = 2)
    private BigDecimal scorePrioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Semaforo semaforo = Semaforo.AMARELO;

    @Column(name = "geojson_polygon", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String geojsonPolygon;

    @Column(name = "ultima_atualizacao", nullable = false)
    private OffsetDateTime ultimaAtualizacao;

    @PrePersist
    void prePersist() {
        this.ultimaAtualizacao = OffsetDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.ultimaAtualizacao = OffsetDateTime.now();
    }
}
