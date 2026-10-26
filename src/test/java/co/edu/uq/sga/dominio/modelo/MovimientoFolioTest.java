package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class MovimientoFolioTest {

    @Test
    void creaMovimientoOriginalSinReferenciaDeReversion() {
        MovimientoFolio movimiento = movimiento("M-1", TipoMovimiento.CARGO, null);

        assertEquals("M-1", movimiento.getCodigo());
        assertEquals(new BigDecimal("125000"), movimiento.getMonto());
        assertNull(movimiento.getCodigoMovimientoReversado());
    }

    @Test
    void exigeReferenciaParaReversiones() {
        assertThrows(
                IllegalArgumentException.class,
                () -> movimiento("M-2", TipoMovimiento.REVERSO_CARGO, null));
    }

    @Test
    void rechazaDatosInvalidos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new MovimientoFolio(
                        "M-1", null, BigDecimal.ONE, "cargo", LocalDateTime.now(), null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new MovimientoFolio(
                        "M-1", TipoMovimiento.CARGO, BigDecimal.ZERO,
                        "cargo", LocalDateTime.now(), null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new MovimientoFolio(
                        "M-1", TipoMovimiento.CARGO, BigDecimal.ONE,
                        "cargo", LocalDateTime.now(), "M-0"));
    }

    private MovimientoFolio movimiento(
            String codigo, TipoMovimiento tipo, String codigoReversado) {
        return new MovimientoFolio(
                codigo,
                tipo,
                new BigDecimal("125000"),
                "movimiento de prueba",
                LocalDateTime.of(2026, 10, 26, 10, 0),
                codigoReversado);
    }
}
