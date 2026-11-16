package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.*;
import co.edu.uq.sga.adaptadores.ColaReservasPendientesPorVencimiento;
import co.edu.uq.sga.adaptadores.IndiceReservasArbol;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import co.edu.uq.sga.dominio.modelo.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ServicioReservasPortalTest {
    @Test
    void registroPortalEncolaVencimientoYConfirmacionRetiraLaVigilancia() {
        ServicioReservas reservas = new ServicioReservas(new IndiceReservasArbol());
        ServicioVencimientoReservas vencimientos = new ServicioVencimientoReservas(
                reservas, new ColaReservasPendientesPorVencimiento(
                        ServicioVencimientoReservas.comparadorPorVencimiento()));
        ServicioReservasPortal portal = new ServicioReservasPortal(reservas, vencimientos);
        Reserva reserva = crearReserva();
        portal.registrarPendiente(reserva);
        assertTrue(portal.tienePendientesPorVencer());

        Folio folio = new Folio(reserva.getFolio());
        folio.registrarMovimiento(new MovimientoFolio("M1", TipoMovimiento.PAGO,
                new BigDecimal("30"), "Anticipo", LocalDateTime.now(), null));
        portal.confirmar(reserva, folio, new BigDecimal("100"));

        assertEquals(co.edu.uq.sga.dominio.enums.EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertFalse(portal.tienePendientesPorVencer());
    }

    private static Reserva crearReserva() {
        Apartamento apartamento = new Apartamento("A1", "Suite", "A", 1, 1, 2,
                new String[0], BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
        Huesped titular = new Huesped("Ana", "123", LocalDate.of(1990, 1, 1),
                "3001234567", "ana@example.com", null);
        return new Reserva("F-PORTAL", titular,
                new Ocupante[]{new Ocupante("Ana", LocalDate.of(1990, 1, 1))},
                new Estancia(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12)),
                CanalReserva.PORTAL,
                new AsignacionApartamento[]{new AsignacionApartamento(apartamento)});
    }
}
