package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FolioTest {

    @Test
    void calculaSaldoConCargosYPagos() {
        Folio folio = new Folio("F-1");
        folio.registrarMovimiento(movimiento("M-1", TipoMovimiento.CARGO, "100000", null));
        folio.registrarMovimiento(movimiento("M-2", TipoMovimiento.PAGO, "30000", null));

        assertEquals(new BigDecimal("70000"), folio.calcularSaldo());
    }

    @Test
    void lasReversionesCorrigenSaldoSinEliminarMovimientos() {
        Folio folio = new Folio("F-1");
        folio.registrarMovimiento(movimiento("M-1", TipoMovimiento.CARGO, "100000", null));
        folio.registrarMovimiento(movimiento("M-2", TipoMovimiento.PAGO, "30000", null));
        folio.registrarMovimiento(
                movimiento("M-3", TipoMovimiento.REVERSO_PAGO, "30000", "M-2"));

        assertEquals(3, folio.getMovimientos().length);
        assertEquals(new BigDecimal("100000"), folio.calcularSaldo());
    }

    @Test
    void revierteCargoYNoPermiteRevertirDosVeces() {
        Folio folio = new Folio("F-1");
        folio.registrarMovimiento(movimiento("M-1", TipoMovimiento.CARGO, "100000", null));
        folio.registrarMovimiento(
                movimiento("M-2", TipoMovimiento.REVERSO_CARGO, "100000", "M-1"));

        assertEquals(BigDecimal.ZERO, folio.calcularSaldo());
        assertThrows(
                IllegalArgumentException.class,
                () -> folio.registrarMovimiento(
                        movimiento("M-3", TipoMovimiento.REVERSO_CARGO, "100000", "M-1")));
    }

    @Test
    void exigeMovimientoOriginalDelMismoFolioTipoYMonto() {
        Folio folio = new Folio("F-1");
        folio.registrarMovimiento(movimiento("M-1", TipoMovimiento.CARGO, "100000", null));

        assertThrows(
                IllegalArgumentException.class,
                () -> folio.registrarMovimiento(
                        movimiento("M-2", TipoMovimiento.REVERSO_CARGO, "50000", "M-1")));
        assertThrows(
                IllegalArgumentException.class,
                () -> folio.registrarMovimiento(
                        movimiento("M-3", TipoMovimiento.REVERSO_PAGO, "100000", "M-1")));
        assertThrows(
                IllegalArgumentException.class,
                () -> folio.registrarMovimiento(
                        movimiento("M-4", TipoMovimiento.REVERSO_CARGO, "100000", "inexistente")));
    }

    @Test
    void protegeElRegistroYRechazaCodigosDuplicados() {
        Folio folio = new Folio("F-1");
        MovimientoFolio original = movimiento("M-1", TipoMovimiento.CARGO, "100000", null);
        folio.registrarMovimiento(original);

        MovimientoFolio[] copia = folio.getMovimientos();
        copia[0] = null;
        assertEquals(original, folio.getMovimientos()[0]);

        assertThrows(
                IllegalArgumentException.class,
                () -> folio.registrarMovimiento(
                        movimiento("M-1", TipoMovimiento.PAGO, "100000", null)));
    }

    @Test
    void registraDevolucionSinBorrarElPagoOriginal() {
        Folio folio = new Folio("F-1");
        folio.registrarMovimiento(movimiento("M-1", TipoMovimiento.PAGO, "30000", null));
        folio.registrarMovimiento(movimiento("M-2", TipoMovimiento.DEVOLUCION, "30000", null));

        assertEquals(2, folio.getMovimientos().length);
        assertEquals(BigDecimal.ZERO, folio.calcularSaldo());
    }

    @Test
    void rechazaCodigoDeFolioVacioYMovimientoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Folio(" "));
        Folio folio = new Folio("F-1");
        assertThrows(IllegalArgumentException.class, () -> folio.registrarMovimiento(null));
    }

    private MovimientoFolio movimiento(
            String codigo, TipoMovimiento tipo, String monto, String codigoReversado) {
        return new MovimientoFolio(
                codigo,
                tipo,
                new BigDecimal(monto),
                "movimiento de prueba",
                LocalDateTime.of(2026, 10, 26, 10, 0),
                codigoReversado);
    }
}
