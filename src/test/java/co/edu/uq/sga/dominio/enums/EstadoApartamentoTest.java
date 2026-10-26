package co.edu.uq.sga.dominio.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EstadoApartamentoTest {

    @Test
    void contieneTodosLosEstadosOperativosDelRequerimiento() {
        assertEquals(5, EstadoApartamento.values().length);
        assertEquals(EstadoApartamento.PREPARADO, EstadoApartamento.valueOf("PREPARADO"));
        assertEquals(EstadoApartamento.OCUPADO, EstadoApartamento.valueOf("OCUPADO"));
        assertEquals(
                EstadoApartamento.PENDIENTE_PREPARACION,
                EstadoApartamento.valueOf("PENDIENTE_PREPARACION"));
        assertEquals(EstadoApartamento.EN_PREPARACION, EstadoApartamento.valueOf("EN_PREPARACION"));
        assertEquals(
                EstadoApartamento.FUERA_DE_SERVICIO,
                EstadoApartamento.valueOf("FUERA_DE_SERVICIO"));
    }
}
