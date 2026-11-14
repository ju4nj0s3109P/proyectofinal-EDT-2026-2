package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.dominio.enums.EstadoTareaPreparacion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class TareaPreparacionTest {
    @Test
    void creaTareaYAplicaTransicionesPermitidas() {
        TareaPreparacion tarea = crearTarea();
        assertEquals(EstadoTareaPreparacion.ENCOLADA, tarea.getEstado());
        tarea.iniciar();
        assertEquals(EstadoTareaPreparacion.EN_PROCESO, tarea.getEstado());
        tarea.completar();
        assertEquals(EstadoTareaPreparacion.COMPLETADA, tarea.getEstado());
    }

    @Test
    void rechazaDatosAusentesYTransicionesInvalidas() {
        assertThrows(IllegalArgumentException.class,
                () -> new TareaPreparacion(" ", crearApartamento(), LocalDateTime.now(), llegada(10)));
        assertThrows(IllegalArgumentException.class,
                () -> new TareaPreparacion("T-1", null, LocalDateTime.now(), llegada(10)));
        assertThrows(IllegalArgumentException.class,
                () -> new TareaPreparacion("T-1", crearApartamento(), null, llegada(10)));
        assertThrows(IllegalArgumentException.class,
                () -> new TareaPreparacion("T-1", crearApartamento(), LocalDateTime.now(), null));
        TareaPreparacion tarea = crearTarea();
        assertThrows(IllegalStateException.class, tarea::completar);
        tarea.iniciar();
        assertThrows(IllegalStateException.class, tarea::iniciar);
    }

    @Test
    void comparaPrioridadDeLlegadasDelDiaYDeDiasPosteriores() {
        LocalDate hoy = LocalDate.of(2026, 11, 14);
        TareaPreparacion llegadaTemprana = crearTarea("T-1", "A-1", llegada(9));
        TareaPreparacion llegadaTardia = crearTarea("T-2", "A-2", llegada(15));
        TareaPreparacion llegadaManana = crearTarea("T-3", "A-3",
                LocalDateTime.of(2026, 11, 15, 8, 0));

        var comparador = TareaPreparacion.comparadorPorPrioridad(hoy);
        assertTrue(comparador.compare(llegadaTemprana, llegadaTardia) < 0);
        assertTrue(comparador.compare(llegadaTardia, llegadaManana) < 0);
    }

    private TareaPreparacion crearTarea() {
        return crearTarea("T-1", "A-1", llegada(10));
    }

    private TareaPreparacion crearTarea(String codigo, String apartamento, LocalDateTime llegada) {
        return new TareaPreparacion(codigo, crearApartamento(apartamento),
                LocalDateTime.of(2026, 11, 13, 8, 0), llegada);
    }

    private LocalDateTime llegada(int hora) {
        return LocalDateTime.of(2026, 11, 14, hora, 0);
    }

    private Apartamento crearApartamento() {
        return crearApartamento("A-1");
    }

    private Apartamento crearApartamento(String codigo) {
        return new Apartamento(codigo, "Suite", "A", 1, 1, 2, new String[0],
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
