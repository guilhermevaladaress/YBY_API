package com.yby.api.job;

import com.yby.api.service.DesmatamentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Carga diaria do DETER (AGENTS.md 5.4: "DETER: carga diaria via @Scheduled quando habilitado
 * no ambiente").
 *
 * <p>O bean so e criado quando {@code app.import.deter.enabled=true}, de modo que a integracao
 * pesada nao roda automaticamente em testes nem em ambientes onde nao foi habilitada. Em
 * hackathon a importacao reusa o fluxo mock de {@link DesmatamentoService}.</p>
 */
@Component
@ConditionalOnProperty(name = "app.import.deter.enabled", havingValue = "true")
public class DeterImportScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeterImportScheduler.class);

    private final DesmatamentoService desmatamentoService;

    public DeterImportScheduler(DesmatamentoService desmatamentoService) {
        this.desmatamentoService = desmatamentoService;
    }

    /** Executa todo dia as 03:00 (horario do servidor). */
    @Scheduled(cron = "${app.import.deter.cron:0 0 3 * * *}")
    public void importarDeterDiario() {
        Long jobId = desmatamentoService.iniciarImportacao();
        log.info("Carga diaria DETER iniciada via @Scheduled. jobId={}", jobId);
    }
}
