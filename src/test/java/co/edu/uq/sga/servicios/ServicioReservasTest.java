package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.dominio.contratos.IndiceReservas;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import co.edu.uq.sga.dominio.excepciones.AnticipoInsuficienteException;
import co.edu.uq.sga.dominio.excepciones.ConflictoDisponibilidadException;
import co.edu.uq.sga.dominio.modelo.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ServicioReservasTest {

    @Test
    void registraPortalPendienteYConfirmaAlAlcanzarAnticipoMinimo() {
        IndiceFalso indice = new IndiceFalso();
        ServicioReservas servicio = new ServicioReservas(indice);
        Reserva reserva = reserva("R-1", CanalReserva.PORTAL, apartamento());

        servicio.registrarPortal(reserva);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        Folio folio = new Folio("R-1");
        folio.registrarMovimiento(movimiento("P-1", TipoMovimiento.PAGO, "300", null));
        assertThrows(AnticipoInsuficienteException.class,
                () -> servicio.confirmarPortal(reserva, folio, new BigDecimal("1001")));

        folio.registrarMovimiento(movimiento("P-2", TipoMovimiento.PAGO, "1", null));
        servicio.confirmarPortal(reserva, folio, new BigDecimal("1001"));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void registraDirectaConfirmadaYExternaSoloCuandoHayDisponibilidad() {
        ServicioReservas servicio = new ServicioReservas(new IndiceFalso());
        Reserva directa = reserva("R-2", CanalReserva.DIRECTA, apartamento());
        servicio.registrarDirecta(directa);
        assertEquals(EstadoReserva.CONFIRMADA, directa.getEstado());

        IndiceFalso indice = new IndiceFalso();
        ServicioReservas externas = new ServicioReservas(indice);
        Reserva externa = reserva("R-3", CanalReserva.EXTERNA, apartamento());
        indice.rechazarSiguiente = true;
        assertFalse(externas.aceptarExterna(externa));
        assertEquals(EstadoReserva.PENDIENTE, externa.getEstado());
        assertTrue(externas.aceptarExterna(externa));
        assertEquals(EstadoReserva.CONFIRMADA, externa.getEstado());
    }

    @Test
    void directaLanzaExcepcionEspecificaCuandoHayConflicto() {
        IndiceFalso indice = new IndiceFalso();
        indice.rechazarSiguiente = true;
        ServicioReservas servicio = new ServicioReservas(indice);
        Reserva reserva = reserva("R-C", CanalReserva.DIRECTA, apartamento());

        assertThrows(ConflictoDisponibilidadException.class,
                () -> servicio.registrarDirecta(reserva));
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
    }

    @Test
    void cancelaYRegistraDevolucionSinBorrarElPago() {
        ServicioReservas servicio = new ServicioReservas(new IndiceFalso());
        Reserva reserva = reserva("R-4", CanalReserva.PORTAL, apartamento());
        servicio.registrarPortal(reserva);
        Folio folio = new Folio("R-4");
        folio.registrarMovimiento(movimiento("P-1", TipoMovimiento.PAGO, "500", null));
        assertEquals(new BigDecimal("-500"), folio.calcularSaldo());
        assertEquals(new BigDecimal("500"), servicio.calcularDevolucion(folio));

        MovimientoFolio devolucion =
                movimiento("D-1", TipoMovimiento.DEVOLUCION, "500", null);
        servicio.cancelar(reserva, folio, devolucion);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(2, folio.getMovimientos().length);
        assertEquals(BigDecimal.ZERO, folio.calcularSaldo());
        assertFalse(((IndiceFalso) getIndice(servicio)).contiene(reserva));
    }

    @Test
    void expiraPendienteJustoAlCumplirVeinticuatroHoras() {
        IndiceFalso indice = new IndiceFalso();
        ServicioReservas servicio = new ServicioReservas(indice);
        LocalDateTime creacion = LocalDateTime.of(2026, 10, 25, 10, 0);
        Reserva reserva = reserva("R-7", CanalReserva.PORTAL, apartamento(), creacion);
        servicio.registrarPortal(reserva);

        assertFalse(servicio.expirarPendiente(reserva, creacion.plusHours(24).minusNanos(1)));
        assertTrue(servicio.expirarPendiente(reserva, creacion.plusHours(24)));
        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertFalse(indice.contiene(reserva));
    }

    @Test
    void noExpiraReservaPortalConfirmada() {
        IndiceFalso indice = new IndiceFalso();
        ServicioReservas servicio = new ServicioReservas(indice);
        LocalDateTime creacion = LocalDateTime.of(2026, 10, 25, 10, 0);
        Reserva reserva = reserva("R-8", CanalReserva.PORTAL, apartamento(), creacion);
        servicio.registrarPortal(reserva);
        Folio folio = new Folio("R-8");
        folio.registrarMovimiento(movimiento("P-8", TipoMovimiento.PAGO, "30", null));
        servicio.confirmarPortal(reserva, folio, new BigDecimal("100"));

        assertThrows(IllegalStateException.class,
                () -> servicio.expirarPendiente(reserva, creacion.plusHours(25)));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertTrue(indice.contiene(reserva));
    }

    @Test
    void checkInYCheckOutCambianReservaApartamentoEIndice() {
        IndiceFalso indice = new IndiceFalso();
        ServicioReservas servicio = new ServicioReservas(indice);
        Apartamento apartamento = apartamento();
        Reserva reserva = reserva("R-5", CanalReserva.DIRECTA, apartamento);
        servicio.registrarDirecta(reserva);

        servicio.registrarCheckIn(reserva);
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertEquals(EstadoApartamento.OCUPADO, apartamento.getEstadoOperativo());

        servicio.registrarCheckOut(reserva);
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, apartamento.getEstadoOperativo());
        assertFalse(indice.contiene(reserva));
    }

    @Test
    void noPermiteCheckInSinPreparacionNiCancelarDuranteLaEstancia() {
        IndiceFalso indice = new IndiceFalso();
        ServicioReservas servicio = new ServicioReservas(indice);
        Apartamento apartamento = apartamento();
        Reserva reserva = reserva("R-6", CanalReserva.DIRECTA, apartamento);
        servicio.registrarDirecta(reserva);
        apartamento.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);

        assertThrows(IllegalStateException.class, () -> servicio.registrarCheckIn(reserva));
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        apartamento.cambiarEstadoOperativo(EstadoApartamento.PREPARADO);
        servicio.registrarCheckIn(reserva);
        assertThrows(IllegalStateException.class,
                () -> servicio.cancelar(reserva, new Folio("R-6"), null));
    }

    private Reserva reserva(String folio, CanalReserva canal, Apartamento apartamento) {
        return reserva(folio, canal, apartamento, LocalDateTime.of(2026, 10, 25, 10, 0));
    }

    private Reserva reserva(
            String folio, CanalReserva canal, Apartamento apartamento, LocalDateTime creacion) {
        Huesped huesped = new Huesped("Huésped", "123", LocalDate.of(1990, 1, 1),
                "3001234567", "huesped@example.com", null);
        Ocupante[] ocupantes = {new Ocupante("Huésped", LocalDate.of(1990, 1, 1))};
        return new Reserva(folio, huesped, ocupantes,
                new Estancia(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12)),
                canal, new AsignacionApartamento[] {new AsignacionApartamento(apartamento)},
                creacion);
    }

    private Apartamento apartamento() {
        return new Apartamento("A-1", "Suite", "A", 1, 1, 2, new String[0],
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private MovimientoFolio movimiento(String codigo, TipoMovimiento tipo, String monto,
            String revertido) {
        return new MovimientoFolio(codigo, tipo, new BigDecimal(monto), "prueba",
                java.time.LocalDateTime.of(2026, 3, 1, 12, 0), revertido);
    }

    private IndiceReservas getIndice(ServicioReservas servicio) {
        try {
            var campo = ServicioReservas.class.getDeclaredField("indice");
            campo.setAccessible(true);
            return (IndiceReservas) campo.get(servicio);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private static final class IndiceFalso implements IndiceReservas {
        private Reserva registrada;
        private boolean rechazarSiguiente;

        @Override
        public boolean agregarSiDisponible(Reserva reserva) {
            if (rechazarSiguiente) {
                rechazarSiguiente = false;
                return false;
            }
            if (registrada != null) {
                return false;
            }
            registrada = reserva;
            return true;
        }

        @Override
        public boolean retirarSiExiste(Reserva reserva) {
            if (!contiene(reserva)) {
                return false;
            }
            registrada = null;
            return true;
        }

        @Override
        public boolean contiene(Reserva reserva) {
            return registrada == reserva;
        }
    }
}
