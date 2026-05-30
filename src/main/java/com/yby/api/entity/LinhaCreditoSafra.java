package com.yby.api.entity;

import com.yby.api.entity.enums.ProgramaSafra;
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

/**
 * Linha de credito rural do Plano Safra (ex.: RenovAgro/ABC+, Pronaf Bioeconomia).
 *
 * <p>Quando {@code exigePraticaCarbono} e verdadeiro, a linha financia praticas de baixa emissao
 * e o {@code fatorReducaoTco2eHa} estima as toneladas de CO2e evitadas/sequestradas por hectare,
 * permitindo simular o duplo beneficio (financiamento + geracao de credito de carbono).</p>
 */
@Getter
@Setter
@Entity
@Table(name = "linhas_credito_safra")
public class LinhaCreditoSafra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProgramaSafra programa;

    /** Ano-safra no formato AAAA/AAAA (ex.: 2025/2026). */
    @Column(name = "ano_safra", nullable = false, length = 9)
    private String anoSafra;

    @Column(name = "instituicao_financeira", length = 150)
    private String instituicaoFinanceira;

    /** Taxa de juros ao ano (fracao, ex.: 0.085 = 8,5% a.a.). */
    @Column(name = "taxa_juros_aa", nullable = false, precision = 6, scale = 4)
    private BigDecimal taxaJurosAa;

    /** Teto de financiamento por beneficiario (R$). */
    @Column(name = "teto_financiamento", precision = 18, scale = 2)
    private BigDecimal tetoFinanciamento;

    @Column(name = "prazo_meses")
    private Integer prazoMeses;

    @Column(name = "carencia_meses")
    private Integer carenciaMeses;

    @Column(name = "exige_pratica_carbono", nullable = false)
    private boolean exigePraticaCarbono;

    /** Reducao/sequestro estimado de CO2e por hectare ao ano (tCO2e/ha). */
    @Column(name = "fator_reducao_tco2e_ha", precision = 10, scale = 4)
    private BigDecimal fatorReducaoTco2eHa;

    @Column(length = 2000)
    private String descricao;

    @Column(nullable = false)
    private boolean ativo = true;

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
