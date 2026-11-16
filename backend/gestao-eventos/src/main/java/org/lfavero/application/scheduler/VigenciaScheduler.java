package org.lfavero.application.scheduler;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.lfavero.application.services.VigenciaService;

/**
 * Dispara a sincronização de vigência dos eventos cadastrados:
 *  1) todo dia logo após a meia-noite (cron configurável);
 *  2) uma vez no startup, para recuperar o atraso caso a aplicação tenha ficado parada.
 */

@ApplicationScoped
public class VigenciaScheduler {

    private static final Logger LOG = Logger.getLogger(VigenciaScheduler.class);

    @Inject
    VigenciaService vigencia;

    @Scheduled(identity = "sincronizar-vigencia-eventos",
            cron = "{eventos.vigencia.cron}",
            timeZone = "{eventos.timezone}",
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void executarDiariamente() {
        vigencia.sincronizar();
    }

    void aoIniciar(@Observes StartupEvent ev) {
        try {
            vigencia.sincronizar();
        } catch (RuntimeException e) {
            LOG.warn("Falha na sincronização de vigência no startup", e);
        }
    }
}
