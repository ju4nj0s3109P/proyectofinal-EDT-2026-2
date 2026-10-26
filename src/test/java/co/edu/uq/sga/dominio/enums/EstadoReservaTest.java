package co.edu.uq.sga.dominio.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EstadoReservaTest {

    @Test
    void defineTodosLosEstadosDelCicloDeVida() {
        assertEquals(6, EstadoReserva.values().length);
        assertEquals(EstadoReserva.PENDIENTE, EstadoReserva.valueOf("PENDIENTE"));
        assertEquals(EstadoReserva.CONFIRMADA, EstadoReserva.valueOf("CONFIRMADA"));
        assertEquals(EstadoReserva.EN_CURSO, EstadoReserva.valueOf("EN_CURSO"));
        assertEquals(EstadoReserva.FINALIZADA, EstadoReserva.valueOf("FINALIZADA"));
        assertEquals(EstadoReserva.CANCELADA, EstadoReserva.valueOf("CANCELADA"));
        assertEquals(EstadoReserva.NO_SHOW, EstadoReserva.valueOf("NO_SHOW"));
    }
}
