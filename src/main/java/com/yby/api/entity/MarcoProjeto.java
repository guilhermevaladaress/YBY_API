package com.yby.api.entity;

import com.yby.api.entity.enums.MarcoStatus;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Marco (etapa) acompanhado dentro de um projeto JREDD+.
 */
@Getter
@Setter
@Entity
@Table(name = "marcos_projeto")
public class MarcoProjeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "projeto_id", nullable = false)
    private ProjetoJredd projeto;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 1500)
    private String descricao;

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MarcoStatus status = MarcoStatus.PENDENTE;

    @Column(name = "percentual_conclusao", nullable = false)
    private Integer percentualConclusao = 0;

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
            this.status = MarcoStatus.PENDENTE;
        }
        if (this.percentualConclusao == null) {
            this.percentualConclusao = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
