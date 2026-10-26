package co.edu.uq.sga.dominio.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import co.edu.uq.sga.dominio.enums.EstadoTareaPreparacion;
import java.math.BigDecimal;
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
                () -> new TareaPreparacion(" ", crearApartamento(), LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class,
                () -> new TareaPreparacion("T-1", null, LocalDateTime.now()));
        assertThrows(IllegalArgumentException.class,
                () -> new TareaPreparacion("T-1", crearApartamento(), null));
        TareaPreparacion tarea = crearTarea();
        assertThrows(IllegalStateException.class, tarea::completar);
        tarea.iniciar();
        assertThrows(IllegalStateException.class, tarea::iniciar);
    }

    private TareaPreparacion crearTarea() {
        return new TareaPreparacion("T-1", crearApartamento(),
                LocalDateTime.of(2026, 10, 26, 10, 0));
    }

    private Apartamento crearApartamento() {
        return new Apartamento("A-1", "Suite", "A", 1, 1, 2, new String[0],
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }
}
