package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class OcupanteTest {

    @Test
    void creaOcupanteYRecortaElNombre() {
        Ocupante ocupante = new Ocupante("  Camila Ruiz  ", LocalDate.of(2020, 4, 27));

        assertEquals("Camila Ruiz", ocupante.getNombre());
        assertEquals(LocalDate.of(2020, 4, 27), ocupante.getFechaNacimiento());
    }

    @Test
    void calculaEdadCumplidaEnLaFechaDeReferencia() {
        Ocupante ocupante = new Ocupante("Camila", LocalDate.of(2020, 4, 27));

        assertEquals(5, ocupante.calcularEdadEn(LocalDate.of(2026, 4, 26)));
        assertEquals(6, ocupante.calcularEdadEn(LocalDate.of(2026, 4, 27)));
        assertEquals(6, ocupante.calcularEdadEn(LocalDate.of(2026, 4, 28)));
    }

    @Test
    void identificaElLimiteDeSeisAnios() {
        Ocupante ocupante = new Ocupante("Camila", LocalDate.of(2020, 4, 27));

        assertTrue(ocupante.esMenorDeSeisEn(LocalDate.of(2026, 4, 26)));
        assertFalse(ocupante.esMenorDeSeisEn(LocalDate.of(2026, 4, 27)));
    }

    @Test
    void rechazaNombreOVencimientoNulosOVacios() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Ocupante(" ", LocalDate.of(2020, 1, 1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new Ocupante("Camila", null));
    }

    @Test
    void rechazaFechaDeNacimientoFutura() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Ocupante("Camila", LocalDate.now().plusDays(1)));
    }

    @Test
    void rechazaFechasDeReferenciaInvalidas() {
        Ocupante ocupante = new Ocupante("Camila", LocalDate.of(2020, 4, 27));

        assertThrows(
                IllegalArgumentException.class,
                () -> ocupante.calcularEdadEn(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> ocupante.calcularEdadEn(LocalDate.of(2020, 4, 26)));
    }
}
