package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.adaptadores.ColaReservasPendientesPorVencimiento;
import co.edu.uq.sga.adaptadores.IndiceReservasArbol;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.AsignacionApartamento;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Huesped;
import co.edu.uq.sga.dominio.modelo.Ocupante;
import co.edu.uq.sga.dominio.modelo.Reserva;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ServicioVencimientoReservasTest {

    private static final LocalDateTime LUNES_10 = LocalDateTime.of(2026, 10, 26, 10, 0);

    private final IndiceReservasArbol indice = new IndiceReservasArbol();
    private final ServicioReservas reservas = new ServicioReservas(indice);
    private final ColaReservasPendientesPorVencimiento cola =
            new ColaReservasPendientesPorVencimiento(
                    ServicioVencimientoReservas.comparadorPorVencimiento());
    private final ServicioVencimientoReservas servicio =
            new ServicioVencimientoReservas(reservas, cola);

    @Test
    void venceLasPendientesEnOrdenDeVencimientoYLiberaSusNoches() {
        Apartamento apartamento = apartamento("A-101");
        Reserva temprana = reserva("R-1", apartamento, LUNES_10, "2026-12-10", "2026-12-12");
        Reserva tardia = reserva("R-2", apartamento, LUNES_10.plusHours(5),
                "2026-12-20", "2026-12-22");
        reservas.registrarPortal(temprana);
        reservas.registrarPortal(tardia);
        servicio.vigilar(temprana);
        servicio.vigilar(tardia);

        assertEquals("R-1", cola.verProxima().getFolio(), "vence antes la que se creó antes");
        assertEquals(0, servicio.procesarVencidas(LUNES_10.plusHours(23)),
                "antes de las 24 horas no vence ninguna");

        assertEquals(1, servicio.procesarVencidas(LUNES_10.plusHours(24)));
        assertEquals(EstadoReserva.CANCELADA, temprana.getEstado());
        assertFalse(indice.contiene(temprana), "la noche queda libre");
        assertEquals(EstadoReserva.PENDIENTE, tardia.getEstado());
        assertTrue(servicio.hayPendientes());

        assertEquals(1, servicio.procesarVencidas(LUNES_10.plusHours(30)));
        assertEquals(EstadoReserva.CANCELADA, tardia.getEstado());
        assertFalse(servicio.hayPendientes());
    }

    @Test
    void noVenceLaQueSeConfirmoMientrasEsperaba() {
        Reserva reserva = reserva("R-3", apartamento("A-102"), LUNES_10,
                "2026-12-10", "2026-12-12");
        reservas.registrarPortal(reserva);
        servicio.vigilar(reserva);
        reserva.confirmar();

        assertEquals(0, servicio.procesarVencidas(LUNES_10.plusHours(48)),
                "ya no está pendiente, solo sale de la cola");
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertTrue(indice.contiene(reserva), "sigue ocupando sus noches");
        assertFalse(servicio.hayPendientes());
    }

    @Test
    void dejaDeVigilarYValidaLoQueRecibe() {
        Reserva portal = reserva("R-4", apartamento("A-103"), LUNES_10,
                "2026-12-10", "2026-12-12");
        reservas.registrarPortal(portal);
        servicio.vigilar(portal);

        assertTrue(servicio.dejarDeVigilar(portal));
        assertFalse(servicio.dejarDeVigilar(portal));
        assertFalse(servicio.hayPendientes());

        Reserva directa = reserva("R-5", apartamento("A-104"), LUNES_10,
                "2026-12-10", "2026-12-12", CanalReserva.DIRECTA);
        assertThrows(IllegalArgumentException.class, () -> servicio.vigilar(directa),
                "el vencimiento automático es solo del portal");
        assertThrows(IllegalArgumentException.class, () -> servicio.vigilar(null));
        assertThrows(IllegalArgumentException.class, () -> servicio.procesarVencidas(null));
        assertThrows(IllegalArgumentException.class,
                () -> new ServicioVencimientoReservas(reservas, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ColaReservasPendientesPorVencimiento(null));

        portal.confirmar();
        assertThrows(IllegalStateException.class, () -> servicio.vigilar(portal),
                "solo se vigila una reserva pendiente");
    }

    private static Apartamento apartamento(String codigo) {
        return new Apartamento(codigo, "Suite", "A", 1, 1, 2, new String[0],
                new BigDecimal("100"), new BigDecimal("120"), new BigDecimal("150"));
    }

    private static Reserva reserva(String folio, Apartamento apartamento,
            LocalDateTime creacion, String entrada, String salida) {
        return reserva(folio, apartamento, creacion, entrada, salida, CanalReserva.PORTAL);
    }

    private static Reserva reserva(String folio, Apartamento apartamento,
            LocalDateTime creacion, String entrada, String salida, CanalReserva canal) {
        Huesped titular = new Huesped("Titular", "109" + folio, LocalDate.of(1990, 1, 1),
                "3001234567", "titular@example.com", null);
        Ocupante[] ocupantes = {new Ocupante("Titular", LocalDate.of(1990, 1, 1))};
        return new Reserva(folio, titular, ocupantes,
                new Estancia(LocalDate.parse(entrada), LocalDate.parse(salida)),
                canal, new AsignacionApartamento[] {new AsignacionApartamento(apartamento)},
                creacion);
    }
}
