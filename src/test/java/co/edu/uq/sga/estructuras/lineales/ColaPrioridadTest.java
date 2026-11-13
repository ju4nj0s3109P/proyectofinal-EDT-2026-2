package co.edu.uq.sga.estructuras.lineales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ColaPrioridadTest {

    private record Tarea(String apto, boolean entradaHoy, LocalTime llegada) {}

    private static final Comparator<Tarea> PRIORIDAD_PREPARACION =
            Comparator.comparing((Tarea t) -> !t.entradaHoy())
                    .thenComparing(Tarea::llegada, Comparator.nullsLast(Comparator.naturalOrder()));

    @Test
    void atiendePrimeroEntradasDeHoyPorHoraDeLlegada() {
        ColaPrioridad<Tarea> cola = new ColaPrioridad<>(PRIORIDAD_PREPARACION);
        cola.insertar(new Tarea("A-101", false, null));
        cola.insertar(new Tarea("B-201", true, LocalTime.of(15, 0)));
        cola.insertar(new Tarea("A-102", false, null));
        cola.insertar(new Tarea("C-301", true, LocalTime.of(11, 30)));
        cola.insertar(new Tarea("B-202", true, LocalTime.of(13, 0)));

        StringBuilder orden = new StringBuilder();
        while (!cola.estaVacia()) {
            orden.append(cola.extraerPrimero().apto()).append(' ');
        }
        // sin entrada hoy: se respeta el orden de llegada a la cola (estable)
        assertEquals("C-301 B-202 B-201 A-101 A-102 ", orden.toString());
    }

    @Test
    void ordenNaturalSacaElMenor() {
        ColaPrioridad<Integer> cola = ColaPrioridad.ordenNatural();
        for (int n : new int[] {5, 3, 8, 1, 9, 2}) {
            cola.insertar(n);
        }
        assertEquals(1, cola.verPrimero());
        assertEquals(6, cola.tamanio());
        assertEquals("[1, 2, 3, 5, 8, 9]", cola.enOrden().toString());
        assertEquals(6, cola.tamanio(), "recorrer no debe vaciar la cola");
    }

    @Test
    void creceYMantieneElOrdenConMuchosElementos() {
        Random azar = new Random(42);
        ColaPrioridad<Integer> cola = ColaPrioridad.ordenNatural();
        ListaEnlazada<Integer> referencia = new ListaEnlazada<>();
        for (int i = 0; i < 500; i++) {
            int n = azar.nextInt(1000);
            cola.insertar(n);
            referencia.agregar(n);
        }
        referencia.ordenar(Comparator.naturalOrder());
        for (int esperado : referencia) {
            assertEquals(esperado, cola.extraerPrimero());
        }
        assertTrue(cola.estaVacia());
    }

    @Test
    void eliminaElementoArbitrarioSinRomperElMonticulo() {
        ColaPrioridad<Integer> cola = ColaPrioridad.ordenNatural();
        for (int n = 1; n <= 10; n++) {
            cola.insertar(n);
        }
        assertTrue(cola.eliminar(4));
        assertTrue(cola.eliminar(1));
        assertTrue(cola.eliminar(10));
        assertFalse(cola.eliminar(42));
        assertFalse(cola.contiene(4));
        assertTrue(cola.contiene(5));
        assertEquals("[2, 3, 5, 6, 7, 8, 9]", cola.enOrden().toString());
    }

    @Test
    void colaVaciaLanzaExcepcionPropia() {
        ColaPrioridad<Integer> cola = ColaPrioridad.ordenNatural();
        assertThrows(EstructuraVaciaException.class, cola::verPrimero);
        assertThrows(EstructuraVaciaException.class, cola::extraerPrimero);
        assertThrows(EstructuraVaciaException.class, () -> cola.iterator().next());
        assertThrows(IllegalArgumentException.class, () -> cola.insertar(null));
    }
}
