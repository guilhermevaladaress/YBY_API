package com.yby.api.service;

import com.yby.api.entity.AuditLog;
import com.yby.api.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tools.jackson.core.JsonProcessingException;
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
        log.setUsuarioEmail(authentication.getName());
        log.setAcao(acao);
        log.setEntidade(entidade);
        log.setEntidadeId(entidadeId);
        log.setPayloadRelevante(asJson(payloadRelevante));
        auditLogRepository.save(log);
    }

    private String asJson(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            return "{\"erro\":\"falha_ao_serializar_payload\"}";
        }
    }
}
