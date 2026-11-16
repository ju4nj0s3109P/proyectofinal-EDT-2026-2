package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.sga.adaptadores.IndiceReservasArbol;
import co.edu.uq.sga.adaptadores.PilaDeshacerFoliosArbol;
import co.edu.uq.sga.adaptadores.RepositorioFoliosArbol;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.enums.TipoTemporada;
import co.edu.uq.sga.dominio.excepciones.AnticipoInsuficienteException;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.AsignacionApartamento;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Folio;
import co.edu.uq.sga.dominio.modelo.Huesped;
import co.edu.uq.sga.dominio.modelo.Ocupante;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.dominio.modelo.Temporada;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Flujo completo del módulo con el índice del árbol y el folio reales: cotizar,
 * cobrar la estancia, pagar el anticipo, confirmar, entrar, salir y cancelar.
 */
class FlujoReservaFolioTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 1, 9, 0);

    private final IndiceReservasArbol indice = new IndiceReservasArbol();
    private final ServicioReservas reservas = new ServicioReservas(indice);
    private final ServicioFolios folios =
            new ServicioFolios(new RepositorioFoliosArbol(), new PilaDeshacerFoliosArbol());
    private final ServicioTarifas tarifas = new ServicioTarifas();

    FlujoReservaFolioTest() {
        tarifas.definirTemporada(new Temporada(TipoTemporada.BAJA,
                LocalDate.of(2026, 12, 1), LocalDate.of(2027, 1, 1)));
    }

    @Test
    void confirmaConElTreintaPorCientoAunqueLaEstanciaYaEsteCobrada() {
        Apartamento apartamento = apartamento("A-101", 2);
        Reserva reserva = reserva("R-1", CanalReserva.PORTAL, apartamento);
        reservas.registrarPortal(reserva);

        BigDecimal total = tarifas.cotizar(apartamento, reserva.getEstancia(),
                reserva.getOcupantes()).getTotal();
        assertEquals(new BigDecimal("200"), total, "2 noches x 100 x 1 ocupante");

        Folio folio = folios.abrir(reserva);
        folios.registrarCargo("R-1", total, "Estadía", AHORA);
        assertThrows(AnticipoInsuficienteException.class,
                () -> reservas.confirmarPortal(reserva, folio, total));

        folios.registrarPago("R-1", new BigDecimal("60"), "Anticipo 30 %", AHORA);
        reservas.confirmarPortal(reserva, folio, total);

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(new BigDecimal("60"), folios.totalPagado("R-1"));
        assertEquals(new BigDecimal("140"), folio.calcularSaldo(), "queda por pagar el 70 %");
    }

    @Test
    void recorreCheckInYCheckOutConElIndiceReal() {
        Apartamento apartamento = apartamento("A-102", 2);
        Reserva reserva = reserva("R-2", CanalReserva.DIRECTA, apartamento);
        reservas.registrarDirecta(reserva);
        Folio folio = folios.abrir(reserva);
        folios.registrarCargo("R-2", new BigDecimal("200"), "Estadía", AHORA);
        folios.registrarPago("R-2", new BigDecimal("200"), "Pago completo", AHORA);

        reservas.registrarCheckIn(reserva);
        assertEquals(EstadoApartamento.OCUPADO, apartamento.getEstadoOperativo());

        reservas.registrarCheckOut(reserva);
        assertEquals(EstadoReserva.FINALIZADA, reserva.getEstado());
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, apartamento.getEstadoOperativo());
        assertFalse(indice.contiene(reserva), "las noches quedan libres");
        assertEquals(0, folio.calcularSaldo().signum());
    }

    @Test
    void devuelveTodoElDineroRecibidoAlCancelarYLiberaLasNoches() {
        Apartamento apartamento = apartamento("A-103", 2);
        Reserva reserva = reserva("R-3", CanalReserva.PORTAL, apartamento);
        reservas.registrarPortal(reserva);
        Folio folio = folios.abrir(reserva);
        folios.registrarCargo("R-3", new BigDecimal("200"), "Estadía", AHORA);
        folios.registrarPago("R-3", new BigDecimal("60"), "Anticipo", AHORA);

        assertEquals(new BigDecimal("60"), reservas.calcularDevolucion(folio));
        assertEquals(new BigDecimal("60"), folios.cerrarPorCancelacion("R-3", AHORA));
        reservas.cancelar(reserva, folio);

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
        assertEquals(0, folios.totalPagado("R-3").signum());
        assertEquals(0, folio.calcularSaldo().signum());
        assertEquals(4, folio.getMovimientos().length);
        assertFalse(indice.contiene(reserva));

        // La misma noche vuelve a venderse después de cancelar.
        Reserva otra = reserva("R-4", CanalReserva.DIRECTA, apartamento);
        reservas.registrarDirecta(otra);
        assertEquals(EstadoReserva.CONFIRMADA, otra.getEstado());
    }

    private static Apartamento apartamento(String codigo, int capacidad) {
        return new Apartamento(codigo, "Suite", "A", 1, 1, capacidad, new String[0],
                new BigDecimal("100"), new BigDecimal("120"), new BigDecimal("150"));
    }

    private static Reserva reserva(String folio, CanalReserva canal, Apartamento apartamento) {
        Huesped titular = new Huesped("Titular", "109" + folio, LocalDate.of(1990, 1, 1),
                "3001234567", "titular@example.com", null);
        Ocupante[] ocupantes = {new Ocupante("Titular", LocalDate.of(1990, 1, 1))};
        return new Reserva(folio, titular, ocupantes,
                new Estancia(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12)),
                canal, new AsignacionApartamento[] {new AsignacionApartamento(apartamento)},
                AHORA);
    }
}
