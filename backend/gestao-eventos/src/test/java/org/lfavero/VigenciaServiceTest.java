package org.lfavero;

import org.junit.jupiter.api.Test;
import org.lfavero.application.services.VigenciaService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VigenciaServiceTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 5, 10);
    private static final LocalDate FIM = LocalDate.of(2026, 5, 20);

    @Test
    void naoEstaVigenteAntesDoInicio() {
        assertFalse(VigenciaService.estaVigente(INICIO, FIM, INICIO.minusDays(1)));
    }

    @Test
    void estaVigenteNoDiaInicial() {
        assertTrue(VigenciaService.estaVigente(INICIO, FIM, INICIO));
    }

    @Test
    void estaVigenteNoDiaFinal() {
        assertTrue(VigenciaService.estaVigente(INICIO, FIM, FIM));
    }

    @Test
    void naoEstaVigenteDepoisDoFim() {
        assertFalse(VigenciaService.estaVigente(INICIO, FIM, FIM.plusDays(1)));
    }
}
