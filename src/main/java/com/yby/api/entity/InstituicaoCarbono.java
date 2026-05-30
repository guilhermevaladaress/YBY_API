package com.yby.api.entity;

import com.yby.api.entity.enums.InstituicaoTipo;
import com.yby.api.entity.enums.PadraoCertificacao;
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
 * Instituicao que compra, certifica, verifica ou intermedeia credito de carbono.
 *
 * <p>Cadastro que viabiliza a integracao do estado com o mercado: o gestor registra
 * compradores/certificadoras (ex.: Verra, Gold Standard, LEAF) com o preco de referencia
 * por tonelada, e o sistema faz o matching com os creditos cadastrados (melhor receita).</p>
 */
@Getter
@Setter
@Entity
@Table(name = "instituicoes_carbono")
public class InstituicaoCarbono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InstituicaoTipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "padrao_certificacao", length = 30)
    private PadraoCertificacao padraoCertificacao;

    @Column(length = 80)
    private String pais;

    /** Preco de referencia pago/praticado por tonelada de CO2e. */
    @Column(name = "preco_referencia_tonelada", precision = 18, scale = 4)
    private BigDecimal precoReferenciaTonelada;

    @Column(nullable = false, length = 3)
    private String moeda = "USD";

    @Column(name = "site_url", length = 300)
    private String siteUrl;

    @Column(name = "api_url", length = 300)
    private String apiUrl;

    @Column(name = "contato_email", length = 150)
    private String contatoEmail;

    /** Indica se a instituicao compra/aceita creditos jurisdicionais REDD+ (JREDD+). */
    @Column(name = "compra_jredd", nullable = false)
    private boolean compraJredd;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(length = 2000)
    private String observacoes;

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
