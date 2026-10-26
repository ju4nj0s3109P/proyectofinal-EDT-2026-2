package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.dominio.contratos.ColaSolicitudesExternas;
import co.edu.uq.sga.dominio.contratos.IndiceReservas;
import co.edu.uq.sga.dominio.contratos.RegistroConflictosReservas;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.modelo.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ServicioSolicitudesExternasTest {
    @Test
    void procesaEnFifoAceptaLaPrimeraYRegistraLaSolicitudEnConflicto() {
        IndiceFalso indice = new IndiceFalso();
        ServicioReservas reservas = new ServicioReservas(indice);
        ColaFalsa cola = new ColaFalsa();
        RegistroFalso registro = new RegistroFalso();
        ServicioSolicitudesExternas servicio =
                new ServicioSolicitudesExternas(reservas, cola, registro);
        Reserva primera = crearReserva("F-1", "A-1");
        Reserva segunda = crearReserva("F-2", "A-1");
        servicio.encolar(primera);
        servicio.encolar(segunda);
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 26, 15, 0);

        var resultadoPrimero = servicio.procesarSiguiente(ahora).orElseThrow();
        assertTrue(resultadoPrimero.aceptada());
        assertEquals(primera, resultadoPrimero.reserva());
        assertEquals(EstadoReserva.CONFIRMADA, primera.getEstado());

        var resultadoSegundo = servicio.procesarSiguiente(ahora).orElseThrow();
        assertFalse(resultadoSegundo.aceptada());
        assertEquals(segunda, resultadoSegundo.reserva());
        assertEquals(EstadoReserva.RECHAZADA, segunda.getEstado());
        assertEquals(1, registro.conflictos.length);
        assertEquals("conflicto de disponibilidad",
                registro.conflictos[0].getMotivo());
        assertEquals("F-2", registro.conflictos[0].getFolioReserva());
        assertTrue(servicio.procesarSiguiente(ahora).isEmpty());
    }

    @Test
    void registraYRechazaSolicitudConApartamentoFueraDeServicio() {
        ColaFalsa cola = new ColaFalsa();
        RegistroFalso registro = new RegistroFalso();
        ServicioSolicitudesExternas servicio = new ServicioSolicitudesExternas(
                new ServicioReservas(new IndiceFalso()), cola, registro);
        Reserva reserva = crearReserva("F-3", "A-3");
        reserva.getAsignaciones()[0].getApartamento().marcarFueraDeServicio("reparación");
        servicio.encolar(reserva);

        var resultado = servicio.procesarSiguiente(
                LocalDateTime.of(2026, 10, 26, 15, 0)).orElseThrow();
        assertFalse(resultado.aceptada());
        assertEquals(EstadoReserva.RECHAZADA, reserva.getEstado());
        assertEquals("apartamento fuera de servicio", registro.conflictos[0].getMotivo());
    }

    @Test
    void rechazaDuplicadosEnColaYCanalesQueNoSeanExternos() {
        ServicioSolicitudesExternas servicio = new ServicioSolicitudesExternas(
                new ServicioReservas(new IndiceFalso()), new ColaFalsa(), new RegistroFalso());
        Reserva externa = crearReserva("F-4", "A-4");
        servicio.encolar(externa);
        assertThrows(IllegalStateException.class, () -> servicio.encolar(externa));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.encolar(crearReserva("F-5", "A-5", CanalReserva.PORTAL)));
    }

    private Reserva crearReserva(String folio, String codigoApartamento) {
        return crearReserva(folio, codigoApartamento, CanalReserva.EXTERNA);
    }

    private Reserva crearReserva(String folio, String codigoApartamento, CanalReserva canal) {
        Huesped huesped = new Huesped("Titular", "CC-" + folio, LocalDate.of(1990, 1, 1),
                "3001234567", "titular@example.com", null);
        Ocupante[] ocupantes = {new Ocupante("Titular", LocalDate.of(1990, 1, 1))};
        Apartamento apartamento = new Apartamento(codigoApartamento, "Suite", "A", 1, 1, 1,
                new String[0], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        return new Reserva(folio, huesped, ocupantes,
                new Estancia(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 2)),
                canal, new AsignacionApartamento[] {new AsignacionApartamento(apartamento)});
    }

    private static final class IndiceFalso implements IndiceReservas {
        private Reserva registrada;

        @Override
        public boolean agregarSiDisponible(Reserva reserva) {
            if (registrada != null) return false;
            registrada = reserva;
            return true;
        }

        @Override
        public boolean retirarSiExiste(Reserva reserva) {
            if (!contiene(reserva)) return false;
            registrada = null;
            return true;
        }

        @Override
        public boolean estaDisponible(Reserva reserva) {
            return registrada == null || registrada.getFolio().equals(reserva.getFolio());
        }

        @Override
        public boolean contiene(Reserva reserva) {
            return registrada == reserva;
        }
    }

    private static final class ColaFalsa implements ColaSolicitudesExternas {
        private Reserva[] reservas = new Reserva[0];

        @Override
        public void encolar(Reserva reserva) {
            Reserva[] nuevas = new Reserva[reservas.length + 1];
            System.arraycopy(reservas, 0, nuevas, 0, reservas.length);
            nuevas[reservas.length] = reserva;
            reservas = nuevas;
        }

        @Override
        public boolean estaVacia() {
            return reservas.length == 0;
        }

        @Override
        public boolean contieneFolio(String folio) {
            for (Reserva reserva : reservas) {
                if (reserva.getFolio().equals(folio)) return true;
            }
            return false;
        }

        @Override
        public Reserva desencolar() {
            if (estaVacia()) throw new IllegalStateException("cola vacía");
            Reserva primera = reservas[0];
            Reserva[] restantes = new Reserva[reservas.length - 1];
            System.arraycopy(reservas, 1, restantes, 0, restantes.length);
            reservas = restantes;
            return primera;
        }
    }

    private static final class RegistroFalso implements RegistroConflictosReservas {
        private ConflictoReserva[] conflictos = new ConflictoReserva[0];

        @Override
        public void registrar(ConflictoReserva conflicto) {
            ConflictoReserva[] nuevos = new ConflictoReserva[conflictos.length + 1];
            System.arraycopy(conflictos, 0, nuevos, 0, conflictos.length);
            nuevos[conflictos.length] = conflicto;
            conflictos = nuevos;
        }
    }
}
