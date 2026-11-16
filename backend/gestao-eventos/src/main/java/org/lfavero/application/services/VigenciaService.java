package org.lfavero.application.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.lfavero.domain.repositories.EventoRepository;

import java.time.Clock;
import java.time.LocalDate;

@ApplicationScoped
public class VigenciaService {

    private static final Logger LOG = Logger.getLogger(VigenciaService.class);

    @Inject
    Clock clock;

    @Inject
    EventoRepository eventos;

    public record Resultado(int ativados, int inativados) {
    }

    public LocalDate hoje() {
        return LocalDate.now(clock);
    }

    public static boolean estaVigente(LocalDate dataInicial, LocalDate dataFinal, LocalDate referencia) {
        return !referencia.isBefore(dataInicial) && !referencia.isAfter(dataFinal);
    }

    public boolean estaVigenteHoje(LocalDate dataInicial, LocalDate dataFinal) {
        return estaVigente(dataInicial, dataFinal, hoje());
    }

    @Transactional
    public Resultado sincronizar() {
        LocalDate hoje = hoje();
        int ativados = eventos.ativarVigentes(hoje);
        int inativados = eventos.inativarForaDaVigencia(hoje);
        if (ativados > 0 || inativados > 0) {
            LOG.infof("Vigência sincronizada para %s: %d ativado(s), %d inativado(s)", hoje, ativados, inativados);
        }
        return new Resultado(ativados, inativados);
    }
}
