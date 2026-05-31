package com.yby.api.service.inteligencia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Agenda o recalculo da inteligencia preditiva (busca focos INPE + serie historica e persiste).
 *
 * <p>Roda uma vez na subida da aplicacao (para que a tabela de projecoes ja nasca preenchida) e,
 * a partir dai, diariamente. Desabilitavel por {@code app.inteligencia.scheduler.enabled=false}
 * (usado nos testes para nao disparar carga nem chamada de rede).</p>
 */
@Component
@ConditionalOnProperty(name = "app.inteligencia.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class InteligenciaScheduler {

    private static final Logger log = LoggerFactory.getLogger(InteligenciaScheduler.class);

    private final RecalculoInteligenciaService recalculoInteligenciaService;

    public InteligenciaScheduler(RecalculoInteligenciaService recalculoInteligenciaService) {
        this.recalculoInteligenciaService = recalculoInteligenciaService;
    }

    /** Popula os snapshots preditivos assim que a aplicacao sobe. */
    @EventListener(ApplicationReadyEvent.class)
    public void aoSubir() {
        executarComProtecao("startup");
    }

    /** Atualizacao diaria as 03:00 (focos INPE recentes + reprocessamento da serie). */
    @Scheduled(cron = "${app.inteligencia.scheduler.cron:0 0 3 * * *}")
    public void diario() {
        executarComProtecao("agendado");
    }

    private void executarComProtecao(String origem) {
        try {
            recalculoInteligenciaService.recalcularTodos();
        } catch (Exception e) {
            log.warn("Recalculo de inteligencia ({}) falhou: {}", origem, e.getMessage());
        }
    }
}
