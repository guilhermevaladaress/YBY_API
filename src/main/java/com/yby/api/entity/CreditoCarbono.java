package com.yby.api.entity;

import com.yby.api.entity.enums.CreditoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Credito rural de carbono cadastrado pelo gestor para uma jurisdicao (municipio).
 *
 * <p>Guarda a meta anual de creditos (tCO2e) a ser cumprida e os parametros financeiros usados
 * para projetar a receita de recebimento nos proximos anos (preco por tonelada, crescimento de
 * preco e expectativa de cumprimento da meta).</p>
 */
@Getter
@Setter
@Entity
@Table(name = "creditos_carbono")
public class CreditoCarbono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "municipio_id", nullable = false)
    private Municipio municipio;

    /** Ano base a partir do qual a projecao e calculada. */
    @Column(name = "ano_base", nullable = false)
    private Integer anoBase;

    /** Meta anual de creditos de carbono (tCO2e) a ser cumprida. */
    @Column(name = "meta_tco2e_ano", nullable = false, precision = 18, scale = 4)
    private BigDecimal metaTco2eAno;

    /** Preco por tonelada de CO2e no ano base (R$). */
    @Column(name = "preco_tonelada", nullable = false, precision = 18, scale = 4)
    private BigDecimal precoTonelada;

    /** Crescimento anual esperado do preco (fracao, ex.: 0.05 = 5% a.a.). */
    @Column(name = "taxa_crescimento_preco", precision = 6, scale = 4)
    private BigDecimal taxaCrescimentoPreco;

    /** Expectativa de cumprimento da meta (fracao 0-1, ex.: 0.80 = 80%). */
    @Column(name = "percentual_cumprimento_meta", precision = 5, scale = 4)
    private BigDecimal percentualCumprimentoMeta;

    /** Quantidade de anos projetados a partir do ano base. */
    @Column(name = "horizonte_anos", nullable = false)
    private Integer horizonteAnos;

    @Column(length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditoStatus status = CreditoStatus.PLANEJADO;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = CreditoStatus.PLANEJADO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
