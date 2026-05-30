package com.yby.api.service;

import com.yby.api.dto.SyncResultDTO;
import com.yby.api.entity.Municipio;
import com.yby.api.integration.ibge.IbgeClient;
import com.yby.api.integration.ibge.IbgeMunicipioResponse;
import com.yby.api.repository.MunicipioRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sincroniza a lista oficial de municipios do IBGE com a base local.
 *
 * <p>Integracao real com a API publica de Localidades do IBGE (AGENTS.md 5.4). Cria os
 * municipios ausentes e atualiza o nome dos existentes, preservando metricas calculadas e
 * edicoes manuais (apenas {@code nome} e tratado como autoritativo da fonte). Toda escrita
 * por GESTOR e auditada conforme RN-007.</p>
 */
@Service
public class IbgeSyncService {

    private final IbgeClient ibgeClient;
    private final MunicipioRepository municipioRepository;
    private final AuditService auditService;

    public IbgeSyncService(IbgeClient ibgeClient,
                           MunicipioRepository municipioRepository,
                           AuditService auditService) {
        this.ibgeClient = ibgeClient;
        this.municipioRepository = municipioRepository;
        this.auditService = auditService;
    }

    @Transactional
    public SyncResultDTO sincronizarMunicipios(String ufSigla) {
        List<IbgeMunicipioResponse> municipiosIbge = ibgeClient.listarMunicipios(ufSigla);

        int inseridos = 0;
        int atualizados = 0;
        int ignorados = 0;
        List<String> avisos = new ArrayList<>();

        for (IbgeMunicipioResponse item : municipiosIbge) {
            String codigoIbge = item.id() == null ? null : String.valueOf(item.id());
            if (codigoIbge == null || codigoIbge.length() != 7 || item.nome() == null) {
                avisos.add("Registro IBGE ignorado (codigo/nome invalido): " + item);
                continue;
            }

            Municipio existente = municipioRepository.findByCodigoIbge(codigoIbge).orElse(null);
            if (existente == null) {
                Municipio novo = new Municipio();
                novo.setCodigoIbge(codigoIbge);
                novo.setNome(item.nome());
                municipioRepository.save(novo);
                inseridos++;
            } else if (!item.nome().equals(existente.getNome())) {
                existente.setNome(item.nome());
                municipioRepository.save(existente);
                atualizados++;
            } else {
                ignorados++;
            }
        }

        SyncResultDTO resultado = new SyncResultDTO(
            "IBGE", municipiosIbge.size(), inseridos, atualizados, ignorados, avisos, OffsetDateTime.now());
        auditService.registrarEscritaGestor("SYNC", "municipios", "IBGE", resultado);
        return resultado;
    }
}
