package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ReservaTest {

    @Test
    void creaReservaPendienteConDatosYAsignaciones() {
        Reserva reserva = crearReserva(
                new Ocupante[] {crearOcupante("Ana"), crearOcupante("Luis")},
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 2))});

        assertEquals("F-100", reserva.getFolio());
        assertEquals("CC-1", reserva.getTitular().getDocumento());
        assertEquals(2, reserva.getOcupantes().length);
        assertEquals(CanalReserva.PORTAL, reserva.getCanal());
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertNotNull(reserva.getFechaHoraCreacion());
        assertEquals(1, reserva.getAsignaciones().length);
    }

    @Test
    void protegeLosArreglosDeOcupantesYAsignaciones() {
        Ocupante[] ocupantes = {crearOcupante("Ana")};
        AsignacionApartamento[] asignaciones = {asignar(crearApartamento("A-1", 1))};
        Reserva reserva = crearReserva(ocupantes, asignaciones);

        ocupantes[0] = null;
        asignaciones[0] = null;
        reserva.getOcupantes()[0] = null;
        reserva.getAsignaciones()[0] = null;

        assertEquals("Ana", reserva.getOcupantes()[0].getNombre());
        assertEquals("A-1", reserva.getAsignaciones()[0].getApartamento().getCodigo());
    }

    @Test
    void permiteAsignarVariosApartamentosSiCubrenLaCapacidad() {
        Reserva reserva = crearReserva(
                new Ocupante[] {
                    crearOcupante("Ana"), crearOcupante("Luis"), crearOcupante("Eva")
                },
                new AsignacionApartamento[] {
                    asignar(crearApartamento("A-1", 2)),
                    asignar(crearApartamento("A-2", 2))
                });

        assertEquals(2, reserva.getAsignaciones().length);
    }

    @Test
    void rechazaDatosObligatoriosAusentes() {
        assertThrows(IllegalArgumentException.class, () -> new Reserva(
                " ", crearTitular(), new Ocupante[] {crearOcupante("Ana")},
                crearEstancia(), CanalReserva.PORTAL,
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 1))}));
        assertThrows(IllegalArgumentException.class, () -> new Reserva(
                "F-1", null, new Ocupante[] {crearOcupante("Ana")},
                crearEstancia(), CanalReserva.PORTAL,
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 1))}));
        assertThrows(IllegalArgumentException.class, () -> new Reserva(
                "F-1", crearTitular(), new Ocupante[0],
                crearEstancia(), CanalReserva.PORTAL,
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 1))}));
        assertThrows(IllegalArgumentException.class, () -> new Reserva(
                "F-1", crearTitular(), new Ocupante[] {crearOcupante("Ana")},
                crearEstancia(), null,
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 1))}));
    }

    @Test
    void rechazaCapacidadInsuficienteYAsignacionesDuplicadas() {
        assertThrows(IllegalArgumentException.class, () -> crearReserva(
                new Ocupante[] {crearOcupante("Ana"), crearOcupante("Luis")},
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 1))}));

        Apartamento mismoCodigo1 = crearApartamento("A-1", 2);
        Apartamento mismoCodigo2 = crearApartamento("A-1", 2);
        assertThrows(IllegalArgumentException.class, () -> crearReserva(
                new Ocupante[] {crearOcupante("Ana")},
                new AsignacionApartamento[] {
                    asignar(mismoCodigo1), asignar(mismoCodigo2)
                }));
    }

    @Test
    void aplicaLasTransicionesPermitidasDelCicloDeVida() {
        Reserva reserva = crearReserva(
                new Ocupante[] {crearOcupante("Ana")},
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 1))});

        reserva.confirmar();
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        reserva.iniciar();
        assertEquals(EstadoReserva.EN_CURSO, reserva.getEstado());
        reserva.finalizar();
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
    }

    @Test
    void permiteCancelarPendienteOConfirmada() {
        Reserva pendiente = crearReserva(
                new Ocupante[] {crearOcupante("Ana")},
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 1))});
        pendiente.cancelar();
        assertEquals(EstadoReserva.CANCELADA, pendiente.getEstado());

        Reserva confirmada = crearReserva(
                new Ocupante[] {crearOcupante("Ana")},
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 1))});
        confirmada.confirmar();
        confirmada.cancelar();
        assertEquals(EstadoReserva.CANCELADA, confirmada.getEstado());
    }

    @Test
    void permiteNoShowSoloDesdeConfirmada() {
        Reserva reserva = crearReserva(
                new Ocupante[] {crearOcupante("Ana")},
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 1))});

        assertThrows(IllegalStateException.class, reserva::marcarNoShow);
        reserva.confirmar();
        reserva.marcarNoShow();
        assertEquals(EstadoReserva.NO_SHOW, reserva.getEstado());
    }

    @Test
    void rechazaUnaReservaPendienteYNoPermiteRechazarOtrosEstados() {
        Reserva pendiente = crearReserva(
                new Ocupante[] {crearOcupante("Ana")},
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 1))});
        pendiente.rechazar();
        assertEquals(EstadoReserva.RECHAZADA, pendiente.getEstado());
        assertThrows(IllegalStateException.class, pendiente::rechazar);

        Reserva confirmada = crearReserva(
                new Ocupante[] {crearOcupante("Luis")},
                new AsignacionApartamento[] {asignar(crearApartamento("A-2", 1))});
        confirmada.confirmar();
        assertThrows(IllegalStateException.class, confirmada::rechazar);
    }

    @Test
    void rechazaTransicionesInvalidas() {
        Reserva reserva = crearReserva(
                new Ocupante[] {crearOcupante("Ana")},
                new AsignacionApartamento[] {asignar(crearApartamento("A-1", 1))});

        assertThrows(IllegalStateException.class, reserva::iniciar);
        assertThrows(IllegalStateException.class, reserva::finalizar);
        reserva.cancelar();
        assertThrows(IllegalStateException.class, reserva::confirmar);
        assertThrows(IllegalStateException.class, reserva::cancelar);
    }

    private Reserva crearReserva(Ocupante[] ocupantes, AsignacionApartamento[] asignaciones) {
        return new Reserva(
                "F-100",
                crearTitular(),
                ocupantes,
                crearEstancia(),
                CanalReserva.PORTAL,
                asignaciones);
    }

    private Huesped crearTitular() {
        return new Huesped(
                "Titular",
                "CC-1",
                LocalDate.of(1990, 1, 1),
                "3001234567",
                "titular@example.com",
                null);
    }

    private Ocupante crearOcupante(String nombre) {
        return new Ocupante(nombre, LocalDate.of(1990, 1, 1));
    }

    private Estancia crearEstancia() {
        return new Estancia(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12));
    }

    private AsignacionApartamento asignar(Apartamento apartamento) {
        return new AsignacionApartamento(apartamento);
    }

    private Apartamento crearApartamento(String codigo, int capacidad) {
        return new Apartamento(
                codigo,
                "Suite",
                "A",
                1,
                1,
                capacidad,
                new String[0],
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO);
    }
}
