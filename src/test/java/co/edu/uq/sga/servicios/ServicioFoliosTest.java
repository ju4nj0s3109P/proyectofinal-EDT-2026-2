package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.sga.adaptadores.PilaDeshacerFoliosArbol;
import co.edu.uq.sga.adaptadores.RepositorioFoliosArbol;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import co.edu.uq.sga.dominio.excepciones.ExcepcionDominio;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.AsignacionApartamento;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Folio;
import co.edu.uq.sga.dominio.modelo.Huesped;
import co.edu.uq.sga.dominio.modelo.MovimientoFolio;
import co.edu.uq.sga.dominio.modelo.Ocupante;
import co.edu.uq.sga.dominio.modelo.Reserva;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ServicioFoliosTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 1, 9, 0);

    private final ServicioFolios servicio =
            new ServicioFolios(new RepositorioFoliosArbol(), new PilaDeshacerFoliosArbol());

    @Test
    void abreUnFolioPorReservaConSuMismoCodigo() {
        Folio folio = servicio.abrir(reserva("R-1"));

        assertEquals("R-1", folio.getCodigo());
        assertSame(folio, servicio.obtener("R-1"));
        assertSame(folio, servicio.obtener("  R-1  "));
        assertThrows(ExcepcionDominio.class, () -> servicio.abrir(reserva("R-1")),
                "una reserva tiene un solo folio");
    }

    @Test
    void registraCargosYPagosSinBorrarMovimientos() {
        servicio.abrir(reserva("R-1"));

        MovimientoFolio cargo = servicio.registrarCargo(
                "R-1", new BigDecimal("1000"), "Estadía", AHORA);
        MovimientoFolio pago = servicio.registrarPago(
                "R-1", new BigDecimal("300"), "Anticipo", AHORA.plusHours(1));

        assertEquals(TipoMovimiento.CARGO, cargo.getTipo());
        assertEquals("R-1-M1", cargo.getCodigo());
        assertEquals(TipoMovimiento.PAGO, pago.getTipo());
        assertEquals("R-1-M2", pago.getCodigo());
        assertEquals(AHORA.plusHours(1), pago.getFechaHora());

        Folio folio = servicio.obtener("R-1");
        assertEquals(2, folio.getMovimientos().length);
        assertEquals(new BigDecimal("700"), folio.calcularSaldo());
    }

    @Test
    void cadaFolioLlevaSusPropiosMovimientos() {
        servicio.abrir(reserva("R-1"));
        servicio.abrir(reserva("R-2"));

        servicio.registrarCargo("R-1", new BigDecimal("100"), "Cargo", AHORA);
        servicio.registrarCargo("R-2", new BigDecimal("200"), "Cargo", AHORA);

        assertEquals("R-2-M1", servicio.obtener("R-2").getMovimientos()[0].getCodigo());
        assertEquals(new BigDecimal("100"), servicio.obtener("R-1").calcularSaldo());
        assertEquals(new BigDecimal("200"), servicio.obtener("R-2").calcularSaldo());
    }

    @Test
    void rechazaFolioInexistenteYMovimientoInvalidoSinTocarElFolio() {
        servicio.abrir(reserva("R-1"));

        assertThrows(ExcepcionDominio.class,
                () -> servicio.registrarPago("NO-EXISTE", BigDecimal.ONE, "Pago", AHORA));
        assertThrows(IllegalArgumentException.class, () -> servicio.obtener(" "));
        // El resto de validaciones del movimiento ya se prueban en MovimientoFolioTest.
        assertThrows(IllegalArgumentException.class,
                () -> servicio.registrarPago("R-1", BigDecimal.ZERO, "Pago", AHORA));
        assertEquals(0, servicio.obtener("R-1").getMovimientos().length);

        assertThrows(IllegalArgumentException.class, () -> servicio.abrir(null));
        assertThrows(IllegalArgumentException.class,
                () -> new ServicioFolios(null, new PilaDeshacerFoliosArbol()));
        assertThrows(IllegalArgumentException.class,
                () -> new ServicioFolios(new RepositorioFoliosArbol(), null));
    }

    @Test
    void deshaceEnOrdenInversoConMovimientosContrariosSinBorrarNada() {
        servicio.abrir(reserva("R-1"));
        servicio.abrir(reserva("R-2"));
        servicio.registrarCargo("R-1", new BigDecimal("1000"), "Estadía", AHORA);
        servicio.registrarPago("R-1", new BigDecimal("300"), "Anticipo", AHORA);

        MovimientoFolio reversoPago = servicio.deshacerUltima("R-1", AHORA.plusHours(1));
        assertEquals(TipoMovimiento.REVERSO_PAGO, reversoPago.getTipo());
        assertEquals("R-1-M2", reversoPago.getCodigoMovimientoReversado());
        assertEquals("R-1-M3", reversoPago.getCodigo());
        assertEquals(new BigDecimal("1000"), servicio.obtener("R-1").calcularSaldo());

        MovimientoFolio reversoCargo = servicio.deshacerUltima("R-1", AHORA.plusHours(2));
        assertEquals(TipoMovimiento.REVERSO_CARGO, reversoCargo.getTipo());
        assertEquals("R-1-M1", reversoCargo.getCodigoMovimientoReversado());
        assertEquals(0, servicio.obtener("R-1").calcularSaldo().signum());
        assertEquals(4, servicio.obtener("R-1").getMovimientos().length);

        assertThrows(ExcepcionDominio.class, () -> servicio.deshacerUltima("R-1", AHORA),
                "los reversos no se apilan");
        assertThrows(ExcepcionDominio.class, () -> servicio.deshacerUltima("R-2", AHORA),
                "cada folio tiene su propia pila");
    }

    @Test
    void siElReversoFallaLaOperacionSigueDisponibleParaDeshacer() {
        servicio.abrir(reserva("R-1"));
        servicio.registrarCargo("R-1", new BigDecimal("1000"), "Estadía", AHORA);

        assertThrows(IllegalArgumentException.class, () -> servicio.deshacerUltima("R-1", null));
        assertEquals(1, servicio.obtener("R-1").getMovimientos().length);

        MovimientoFolio reverso = servicio.deshacerUltima("R-1", AHORA);
        assertEquals("R-1-M1", reverso.getCodigoMovimientoReversado());
    }

    private static Reserva reserva(String folio) {
        Huesped titular = new Huesped("Titular", "1094000000", LocalDate.of(1990, 1, 1),
                "3001234567", "titular@example.com", null);
        Ocupante[] ocupantes = {new Ocupante("Titular", LocalDate.of(1990, 1, 1))};
        Apartamento apartamento = new Apartamento("A-101", "Suite", "A", 1, 1, 2, new String[0],
                new BigDecimal("100"), new BigDecimal("120"), new BigDecimal("150"));
        return new Reserva(folio, titular, ocupantes,
                new Estancia(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12)),
                CanalReserva.PORTAL,
                new AsignacionApartamento[] {new AsignacionApartamento(apartamento)},
                AHORA);
    }
}
