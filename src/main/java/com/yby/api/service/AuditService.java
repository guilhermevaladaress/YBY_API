package com.yby.api.service;

import com.yby.api.entity.AuditLog;
import com.yby.api.repository.AuditLogRepository;
import java.time.OffsetDateTime;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    public void registrarEscritaGestor(String acao, String entidade, String entidadeId, Object payloadRelevante) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        boolean gestor = authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_GESTOR".equals(authority.getAuthority()));

        if (!gestor) {
            return;
        }

        AuditLog log = new AuditLog();
        log.setUsuarioId(-1L);
        log.setUsuarioEmail(authentication.getName());
        log.setAcao(acao);
        log.setEntidadeAfetada(entidade + ":" + entidadeId);
        log.setEventoEm(OffsetDateTime.now());
        log.setPayload(asJson(payloadRelevante));
        auditLogRepository.save(log);
    }

    /**
     * RN-007-A - Auditoria Ambiental Estendida.
     *
     * <p>Registra explicitamente o estado antes/depois da alteracao, a fonte
     * ({@code MANUAL|INTEGRACAO|RECALCULO}) e a justificativa, permitindo rastrear
     * decisoes e mudancas manuais (Lei 12.527/2011 - LAI).</p>
     */
    public void registrarAlteracao(String acao, String entidade, String entidadeId,
                                   Object antes, Object depois, FonteAlteracao fonte, String justificativa) {
        registrarEscritaGestor(acao, entidade, entidadeId,
            new AlteracaoDetalhada(antes, depois, fonte == null ? null : fonte.name(), justificativa));
    }

    /** Origem de uma alteracao auditada (RN-007-A). */
    public enum FonteAlteracao {
        MANUAL,
        INTEGRACAO,
        RECALCULO
    }

    private record AlteracaoDetalhada(Object antes, Object depois, String fonte, String justificativa) {
    }

    private String asJson(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{\"erro\":\"falha_ao_serializar_payload\"}";
        }
    }
}
