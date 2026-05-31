package com.yby.api.service;

import com.yby.api.dto.SyncResultDTO;
import com.yby.api.entity.EmissaoCarbono;
import com.yby.api.exception.UnprocessableEntityException;
import com.yby.api.repository.EmissaoCarbonoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sincroniza/atualiza a serie de emissoes de carbono (CO2e) por municipio para um ano.
 *
 * <p>Complementa o baseline carregado por Flyway (V2 = SEEG 2024 real; V8 = serie retroprojetada
 * 2020-2023). Aqui o GESTOR pode (re)gerar qualquer ano sob demanda. A fonte oficial de CO2e
 * municipal e o SEEG (https://seeg.eco.br); enquanto nao ha endpoint REST publico por municipio,
 * o ano e derivado do ano-ancora REAL de 2024 pelo indice estadual do Tocantins (~3%/ano,
 * tendencia puxada por agropecuaria e mudanca de uso da terra). O metodo {@link #fatorEstadual(int)}
 * isola essa regra para que uma ingestao oficial por ano possa substitui-la sem mudar o restante.</p>
 */
@Service
public class EmissaoCarbonoSyncService {

    private static final Logger log = LoggerFactory.getLogger(EmissaoCarbonoSyncService.class);

    /** Ano-ancora com dados municipais REAIS do SEEG (carregado na V2). */
    static final int ANO_ANCORA = 2024;
    static final String FONTE_ANCORA = "SEEG v13.0";
    static final String FONTE_DERIVADA = "SEEG v13.0 (serie sincronizada)";
    /** Crescimento anual medio das emissoes estaduais do TO (indice SEEG). */
    private static final BigDecimal CRESCIMENTO_ANUAL = new BigDecimal("0.03");

    private final EmissaoCarbonoRepository emissaoCarbonoRepository;
    private final AuditService auditService;

    public EmissaoCarbonoSyncService(EmissaoCarbonoRepository emissaoCarbonoRepository,
                                     AuditService auditService) {
        this.emissaoCarbonoRepository = emissaoCarbonoRepository;
        this.auditService = auditService;
    }

    /**
     * (Re)gera a serie de emissoes do ano informado para todos os municipios com ancora 2024.
     * Idempotente: remove as linhas derivadas do ano antes de reinserir.
     */
    @Transactional
    public SyncResultDTO sincronizarAno(Integer ano) {
        int anoRef = ano == null ? Year.now().getValue() : ano;
        if (anoRef < 1990 || anoRef > Year.now().getValue() + 5) {
            throw new UnprocessableEntityException(
                "Ano fora do intervalo suportado (1990.." + (Year.now().getValue() + 5) + ").");
        }
        if (anoRef == ANO_ANCORA) {
            throw new UnprocessableEntityException(
                "O ano-ancora " + ANO_ANCORA + " contem dados reais do SEEG e nao deve ser sobrescrito.");
        }

        LocalDate dataAlvo = LocalDate.of(anoRef, 12, 31);
        List<EmissaoCarbono> ancora = emissaoCarbonoRepository
            .findByDataReferenciaBetween(LocalDate.of(ANO_ANCORA, 12, 31), LocalDate.of(ANO_ANCORA, 12, 31))
            .stream()
            .filter(e -> FONTE_ANCORA.equals(e.getFonte()))
            .toList();

        if (ancora.isEmpty()) {
            throw new UnprocessableEntityException(
                "Ancora SEEG " + ANO_ANCORA + " ausente; rode a carga inicial (Flyway V2) antes de sincronizar.");
        }

        BigDecimal fator = fatorEstadual(anoRef);
        List<String> avisos = new ArrayList<>();

        // Remove geracao anterior do mesmo ano para garantir idempotencia.
        emissaoCarbonoRepository.deleteByDataReferencia(dataAlvo);

        int inseridos = 0;
        for (EmissaoCarbono base : ancora) {
            EmissaoCarbono nova = new EmissaoCarbono();
            nova.setMunicipio(base.getMunicipio());
            nova.setDataReferencia(dataAlvo);
            nova.setEmissaoTco2e(base.getEmissaoTco2e().multiply(fator).setScale(4, RoundingMode.HALF_UP));
            nova.setFonte(FONTE_DERIVADA);
            nova.setObservacao("Derivado do ano-ancora real SEEG " + ANO_ANCORA
                + " pelo indice estadual TO (fator " + fator.toPlainString() + ").");
            emissaoCarbonoRepository.save(nova);
            inseridos++;
        }

        log.info("Emissoes sincronizadas para {}: {} municipios (fator {}).", anoRef, inseridos, fator);
        SyncResultDTO resultado = new SyncResultDTO(
            FONTE_DERIVADA, ancora.size(), inseridos, 0, 0, avisos, OffsetDateTime.now());
        auditService.registrarEscritaGestor("SYNC", "emissoes_carbono", "SEEG-" + anoRef, resultado);
        return resultado;
    }

    /** Indice estadual SEEG-TO (CO2e) relativo a 2024 = 1,00, ~3%/ano. */
    BigDecimal fatorEstadual(int ano) {
        int delta = ano - ANO_ANCORA;
        double fator = Math.pow(1.0 + CRESCIMENTO_ANUAL.doubleValue(), delta);
        return BigDecimal.valueOf(fator).setScale(4, RoundingMode.HALF_UP);
    }
}
