package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EstanciaTest {

    @Test
    void cuentaNochesSinIncluirLaFechaDeSalida() {
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12));

        assertEquals(2, estancia.cantidadNoches());
        assertTrue(estancia.incluyeNoche(LocalDate.of(2026, 3, 10)));
        assertTrue(estancia.incluyeNoche(LocalDate.of(2026, 3, 11)));
        assertFalse(estancia.incluyeNoche(LocalDate.of(2026, 3, 12)));
    }

    @Test
    void rechazaFechasNulasOSalidaAnteriorOIgualALaEntrada() {
        LocalDate entrada = LocalDate.of(2026, 3, 10);

        assertThrows(IllegalArgumentException.class, () -> new Estancia(null, entrada));
        assertThrows(IllegalArgumentException.class, () -> new Estancia(entrada, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Estancia(entrada, entrada));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Estancia(entrada, entrada.minusDays(1)));
    }

    @Test
    void consideraQueLasEstanciasConsecutivasNoSeSolapan() {
        Estancia primera = new Estancia(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12));
        Estancia siguiente = new Estancia(
                LocalDate.of(2026, 3, 12),
                LocalDate.of(2026, 3, 15));

        assertFalse(primera.seSolapaCon(siguiente));
        assertFalse(siguiente.seSolapaCon(primera));
    }

    @Test
    void detectaSolapamientosParcialesYTotales() {
        Estancia base = new Estancia(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 15));
        Estancia parcial = new Estancia(
                LocalDate.of(2026, 3, 14),
                LocalDate.of(2026, 3, 18));
        Estancia contenida = new Estancia(
                LocalDate.of(2026, 3, 11),
                LocalDate.of(2026, 3, 13));

        assertTrue(base.seSolapaCon(parcial));
        assertTrue(base.seSolapaCon(contenida));
    }

    @Test
    void validaParametrosDeConsulta() {
        Estancia estancia = new Estancia(
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12));

        assertThrows(IllegalArgumentException.class, () -> estancia.incluyeNoche(null));
        assertThrows(IllegalArgumentException.class, () -> estancia.seSolapaCon(null));
    }
}
