package co.edu.uq.sga.interfaz;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.servicios.ServicioPanelReservas.ResumenReserva;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PanelCalendarioTest {

    private static final LocalDate ENTRADA = LocalDate.of(2026, 11, 20);
    private static final LocalDate SALIDA = LocalDate.of(2026, 11, 23);

    @Test
    void incluyeLaNocheDeEntradaYExcluyeLaFechaDeSalida() {
        ResumenReserva reserva = reserva(EstadoReserva.CONFIRMADA, "A101");

        assertTrue(PanelCalendario.reservaOcupa(reserva, "A101", ENTRADA));
        assertTrue(PanelCalendario.reservaOcupa(
                reserva, "A101", LocalDate.of(2026, 11, 22)));
        assertFalse(PanelCalendario.reservaOcupa(reserva, "A101", SALIDA));
        assertFalse(PanelCalendario.reservaOcupa(
                reserva, "A101", ENTRADA.minusDays(1)));
    }

    @Test
    void soloMarcaOcupadoElApartamentoAsignado() {
        ResumenReserva reserva = reserva(EstadoReserva.CONFIRMADA, "A101, A103");

        assertTrue(PanelCalendario.reservaOcupa(reserva, "A101", ENTRADA));
        assertTrue(PanelCalendario.reservaOcupa(reserva, "A103", ENTRADA));
        assertFalse(PanelCalendario.reservaOcupa(reserva, "A102", ENTRADA));
    }

    @Test
    void ignoraReservasCanceladasRechazadasYNoShow() {
        assertFalse(PanelCalendario.reservaOcupa(
                reserva(EstadoReserva.CANCELADA, "A101"), "A101", ENTRADA));
        assertFalse(PanelCalendario.reservaOcupa(
                reserva(EstadoReserva.RECHAZADA, "A101"), "A101", ENTRADA));
        assertFalse(PanelCalendario.reservaOcupa(
                reserva(EstadoReserva.NO_SHOW, "A101"), "A101", ENTRADA));
    }

    private ResumenReserva reserva(EstadoReserva estado, String apartamentos) {
        return new ResumenReserva("RES-TEST", "Huesped de prueba", apartamentos,
                ENTRADA, SALIDA, LocalDateTime.of(2026, 11, 20, 15, 0),
                estado, 2, CanalReserva.DIRECTA);
    }
}
