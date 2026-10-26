package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class HuespedTest {

    @Test
    void creaHuespedConSusDatosYFotografia() {
        Huesped huesped = crearHuesped(" fotos/ana.png ");

        assertEquals("Ana Pérez", huesped.getNombre());
        assertEquals("CC-12345", huesped.getDocumento());
        assertEquals(LocalDate.of(2000, 5, 10), huesped.getFechaNacimiento());
        assertEquals(" +57 300 123 4567 ", huesped.getTelefono());
        assertEquals("ana@example.com", huesped.getCorreo());
        assertEquals("fotos/ana.png", huesped.getRutaFotografia());
    }

    @Test
    void permiteFotografiaOpcionalNulaOVacia() {
        assertNull(crearHuesped(null).getRutaFotografia());
        assertNull(crearHuesped("  ").getRutaFotografia());
    }

    @Test
    void eliminaEspaciosExternosDeLosDatosTextuales() {
        Huesped huesped = new Huesped(
                " Ana Pérez ",
                " CC-12345 ",
                LocalDate.of(2000, 5, 10),
                " 3001234567 ",
                " ana@example.com ",
                null);

        assertEquals("Ana Pérez", huesped.getNombre());
        assertEquals("CC-12345", huesped.getDocumento());
        assertEquals("3001234567", huesped.getTelefono());
        assertEquals("ana@example.com", huesped.getCorreo());
    }

    @Test
    void rechazaDatosObligatoriosVacios() {
        assertThrows(IllegalArgumentException.class, () -> new Huesped(
                " ", "CC-1", LocalDate.of(2000, 1, 1), "3001234567", "a@b.co", null));
        assertThrows(IllegalArgumentException.class, () -> new Huesped(
                "Ana", " ", LocalDate.of(2000, 1, 1), "3001234567", "a@b.co", null));
        assertThrows(IllegalArgumentException.class, () -> new Huesped(
                "Ana", "CC-1", LocalDate.of(2000, 1, 1), " ", "a@b.co", null));
    }

    @Test
    void rechazaFechaDeNacimientoNulaOFutura() {
        assertThrows(IllegalArgumentException.class, () -> new Huesped(
                "Ana", "CC-1", null, "3001234567", "a@b.co", null));
        assertThrows(IllegalArgumentException.class, () -> new Huesped(
                "Ana", "CC-1", LocalDate.now().plusDays(1), "3001234567", "a@b.co", null));
    }

    @Test
    void rechazaCorreosConFormatoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Huesped(
                "Ana", "CC-1", LocalDate.of(2000, 1, 1), "3001234567", "sin-arroba", null));
        assertThrows(IllegalArgumentException.class, () -> new Huesped(
                "Ana", "CC-1", LocalDate.of(2000, 1, 1), "3001234567", "ana@dominio", null));
        assertThrows(IllegalArgumentException.class, () -> new Huesped(
                "Ana", "CC-1", LocalDate.of(2000, 1, 1), "3001234567", "ana @dominio.co", null));
    }

    private Huesped crearHuesped(String fotografia) {
        return new Huesped(
                "Ana Pérez",
                "CC-12345",
                LocalDate.of(2000, 5, 10),
                " +57 300 123 4567 ",
                "ana@example.com",
                fotografia);
    }
}
