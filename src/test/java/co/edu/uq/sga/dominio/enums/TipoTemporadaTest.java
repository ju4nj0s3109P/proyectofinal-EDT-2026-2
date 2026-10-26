package co.edu.uq.sga.dominio.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TipoTemporadaTest {

    @Test
    void defineLasTresTemporadasDelAlojamiento() {
        assertEquals(3, TipoTemporada.values().length);
        assertEquals(TipoTemporada.BAJA, TipoTemporada.valueOf("BAJA"));
        assertEquals(TipoTemporada.MEDIA, TipoTemporada.valueOf("MEDIA"));
        assertEquals(TipoTemporada.ALTA, TipoTemporada.valueOf("ALTA"));
    }
}
