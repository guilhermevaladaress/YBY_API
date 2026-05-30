package com.yby.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_email", nullable = false, length = 180)
    private String usuarioEmail;

    @Column(nullable = false, length = 60)
    private String acao;

    @Column(nullable = false, length = 80)
    private String entidade;

    @Column(name = "entidade_id", length = 80)
    private String entidadeId;

    @Column(name = "payload_relevante", columnDefinition = "TEXT")
    private String payloadRelevante;

    @Column(nullable = false)
    private OffsetDateTime timestamp;

    @PrePersist
    void prePersist() {
        this.timestamp = OffsetDateTime.now();
    }
}
