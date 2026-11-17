package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.adaptadores.PilaDeshacerFoliosArbol;
import co.edu.uq.sga.adaptadores.RepositorioFoliosArbol;
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
    void consultaDisponibilidadSinModificarLaReserva() {
        IndiceFalso indice = new IndiceFalso();
        ServicioReservas servicio = new ServicioReservas(indice);
        Reserva reserva = reserva("R-D", CanalReserva.PORTAL, apartamento());

        assertTrue(servicio.consultarDisponibilidad(reserva));
        assertFalse(indice.contiene(reserva));
        indice.conflictoConsulta = true;
        assertFalse(servicio.consultarDisponibilidad(reserva));

        Apartamento fueraDeServicio = apartamento();
        fueraDeServicio.marcarFueraDeServicio("mantenimiento");
        Reserva noDisponible = reserva("R-F", CanalReserva.PORTAL, fueraDeServicio);
        indice.conflictoConsulta = false;
        assertFalse(servicio.consultarDisponibilidad(noDisponible));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.consultarDisponibilidad(null));
    }

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
    void noCancelaHastaDevolverElDineroRecibidoYNoBorraElPago() {
        IndiceFalso indice = new IndiceFalso();
        ServicioReservas servicio = new ServicioReservas(indice);
        ServicioFolios folios = new ServicioFolios(
                new RepositorioFoliosArbol(), new PilaDeshacerFoliosArbol());
        Reserva reserva = reserva("R-4", CanalReserva.PORTAL, apartamento());
        servicio.registrarPortal(reserva);
        Folio folio = folios.abrir(reserva);
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 26, 9, 0);
        folios.registrarCargo("R-4", new BigDecimal("1000"), "Estadía", ahora);
        folios.registrarPago("R-4", new BigDecimal("500"), "Anticipo", ahora);

        assertEquals(new BigDecimal("500"), servicio.calcularDevolucion(folio),
                "la devolución es el dinero recibido, no el saldo del folio");
        assertThrows(IllegalStateException.class, () -> servicio.cancelar(reserva, folio),
                "no se cancela mientras el alojamiento tenga plata del huésped");
        assertTrue(indice.contiene(reserva));

        assertEquals(new BigDecimal("500"), folios.cerrarPorCancelacion("R-4", ahora));
        servicio.cancelar(reserva, folio);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(4, folio.getMovimientos().length,
                "cargo, pago, reverso del cargo y devolución: nada se borró");
        assertEquals(0, folio.calcularSaldo().signum());
        assertFalse(indice.contiene(reserva));
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
                () -> servicio.cancelar(reserva, new Folio("R-6")));
    }

    @Test
    void permiteCheckInParcialYActivaLaReservaAlIngresarElUltimoApartamento() {
        IndiceFalso indice = new IndiceFalso();
        ServicioReservas servicio = new ServicioReservas(indice);
        Apartamento primero = apartamento();
        Apartamento segundo = new Apartamento("A-2", "Suite", "A", 1, 1, 2,
                new String[0], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        Reserva reserva = reservaConApartamentos("R-9", primero, segundo);
        servicio.registrarDirecta(reserva);
        segundo.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);

        servicio.registrarCheckIn(reserva);

        assertEquals(EstadoApartamento.OCUPADO, primero.getEstadoOperativo());
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, segundo.getEstadoOperativo());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertTrue(indice.contiene(reserva));

        segundo.cambiarEstadoOperativo(EstadoApartamento.PREPARADO);
        servicio.registrarCheckIn(reserva);

        assertEquals(EstadoApartamento.OCUPADO, segundo.getEstadoOperativo());
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertTrue(indice.contiene(reserva));
    }

    @Test
    void checkInParcialNoModificaNingunApartamentoSiOtroNoEstaListo() {
        ServicioReservas servicio = new ServicioReservas(new IndiceFalso());
        Apartamento primero = apartamento();
        Apartamento segundo = new Apartamento("A-2", "Suite", "A", 1, 1, 2,
                new String[0], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        Reserva reserva = reservaConApartamentos("R-10", primero, segundo);
        servicio.registrarDirecta(reserva);
        segundo.marcarFueraDeServicio("mantenimiento");

        assertThrows(IllegalStateException.class, () -> servicio.registrarCheckIn(reserva));
        assertEquals(EstadoApartamento.PREPARADO, primero.getEstadoOperativo());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void noPermiteRepetirCheckInCuandoTodosLosApartamentosYaEstanOcupados() {
        ServicioReservas servicio = new ServicioReservas(new IndiceFalso());
        Apartamento primero = apartamento();
        Apartamento segundo = new Apartamento("A-2", "Suite", "A", 1, 1, 2,
                new String[0], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        Reserva reserva = reservaConApartamentos("R-11", primero, segundo);
        servicio.registrarDirecta(reserva);
        servicio.registrarCheckIn(reserva);
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        assertThrows(IllegalStateException.class, () -> servicio.registrarCheckIn(reserva));
    }

    private Reserva reservaConApartamentos(String folio, Apartamento... apartamentos) {
        Huesped huesped = new Huesped("Huesped", "123", LocalDate.of(1990, 1, 1),
                "3001234567", "huesped@example.com", null);
        Ocupante[] ocupantes = {new Ocupante("Huesped", LocalDate.of(1990, 1, 1))};
        AsignacionApartamento[] asignaciones = new AsignacionApartamento[apartamentos.length];
        for (int i = 0; i < apartamentos.length; i++) {
            asignaciones[i] = new AsignacionApartamento(apartamentos[i]);
        }
        return new Reserva(folio, huesped, ocupantes,
                new Estancia(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12)),
                CanalReserva.DIRECTA, asignaciones,
                LocalDateTime.of(2026, 10, 25, 10, 0));
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

    private static final class IndiceFalso implements IndiceReservas {
        private Reserva registrada;
        private boolean rechazarSiguiente;
        private boolean conflictoConsulta;

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
        public boolean estaDisponible(Reserva reserva) {
            return !conflictoConsulta
                    && (registrada == null
                            || registrada.getFolio().equals(reserva.getFolio()));
        }

        @Override
        public boolean contiene(Reserva reserva) {
            return registrada == reserva;
        }
    }
}
