package co.edu.uq.sga.dominio.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TipoMovimientoTest {

    @Test
    void defineMovimientosOriginalesYReversiones() {
        assertEquals(4, TipoMovimiento.values().length);
        assertEquals(TipoMovimiento.CARGO, TipoMovimiento.valueOf("CARGO"));
        assertEquals(TipoMovimiento.PAGO, TipoMovimiento.valueOf("PAGO"));
        assertEquals(TipoMovimiento.REVERSO_CARGO, TipoMovimiento.valueOf("REVERSO_CARGO"));
        assertEquals(TipoMovimiento.REVERSO_PAGO, TipoMovimiento.valueOf("REVERSO_PAGO"));
    }
}
