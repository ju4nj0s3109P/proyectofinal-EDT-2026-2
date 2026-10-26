package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class AsignacionApartamentoTest {

    @Test
    void conservaElApartamentoAsignado() {
        Apartamento apartamento = crearApartamento("A-1");
        AsignacionApartamento asignacion = new AsignacionApartamento(apartamento);

        assertEquals(apartamento, asignacion.getApartamento());
    }

    @Test
    void rechazaUnApartamentoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new AsignacionApartamento(null));
    }

    private Apartamento crearApartamento(String codigo) {
        return new Apartamento(
                codigo,
                "Suite",
                "A",
                1,
                1,
                2,
                new String[0],
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO);
    }
}
