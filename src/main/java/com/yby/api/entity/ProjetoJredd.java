package com.yby.api.entity;

import com.yby.api.entity.enums.ProjetoStatus;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Projeto JREDD+ de gestao ambiental, com meta de carbono, orcamento e marcos de execucao.
 */
@Getter
@Setter
@Entity
@Table(name = "projetos_jredd")
public class ProjetoJredd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 2000)
    private String descricao;

    /** Municipio associado (opcional: projetos podem ser jurisdicionais/estaduais). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjetoStatus status = ProjetoStatus.PLANEJADO;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim_prevista")
    private LocalDate dataFimPrevista;

    /** Meta total de carbono do projeto (tCO2e). */
    @Column(name = "meta_tco2e", precision = 18, scale = 4)
    private BigDecimal metaTco2e;

    @Column(name = "orcamento_previsto", precision = 18, scale = 2)
    private BigDecimal orcamentoPrevisto;

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataPrevista ASC, id ASC")
    private List<MarcoProjeto> marcos = new ArrayList<>();

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
            this.status = ProjetoStatus.PLANEJADO;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
