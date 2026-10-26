package co.edu.uq.sga.dominio.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CanalReservaTest {

    @Test
    void defineLosTresCanalesDeReserva() {
        assertEquals(3, CanalReserva.values().length);
        assertEquals(CanalReserva.PORTAL, CanalReserva.valueOf("PORTAL"));
        assertEquals(CanalReserva.DIRECTA, CanalReserva.valueOf("DIRECTA"));
        assertEquals(CanalReserva.EXTERNA, CanalReserva.valueOf("EXTERNA"));
    }
}
