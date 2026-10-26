package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.dominio.enums.TipoTemporada;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TemporadaTest {

    @Test
    void incluyeInicioYExcluyeLaFechaFinal() {
        Temporada temporada = new Temporada(
                TipoTemporada.ALTA,
                LocalDate.of(2026, 12, 20),
                LocalDate.of(2027, 1, 10));

        assertEquals(TipoTemporada.ALTA, temporada.getTipo());
        assertTrue(temporada.incluyeNoche(LocalDate.of(2026, 12, 20)));
        assertTrue(temporada.incluyeNoche(LocalDate.of(2027, 1, 9)));
        assertFalse(temporada.incluyeNoche(LocalDate.of(2027, 1, 10)));
        assertFalse(temporada.incluyeNoche(LocalDate.of(2026, 12, 19)));
    }

    @Test
    void rechazaTipoYFechasInvalidas() {
        LocalDate inicio = LocalDate.of(2026, 1, 1);
        LocalDate fin = LocalDate.of(2026, 2, 1);

        assertThrows(IllegalArgumentException.class, () -> new Temporada(null, inicio, fin));
        assertThrows(IllegalArgumentException.class, () -> new Temporada(TipoTemporada.BAJA, null, fin));
        assertThrows(IllegalArgumentException.class, () -> new Temporada(TipoTemporada.BAJA, inicio, null));
        assertThrows(IllegalArgumentException.class, () -> new Temporada(TipoTemporada.BAJA, inicio, inicio));
        assertThrows(IllegalArgumentException.class, () -> new Temporada(TipoTemporada.BAJA, fin, inicio));
    }

    @Test
    void rechazaFechaConsultadaNula() {
        Temporada temporada = new Temporada(
                TipoTemporada.BAJA,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1));

        assertThrows(IllegalArgumentException.class, () -> temporada.incluyeNoche(null));
    }
}
