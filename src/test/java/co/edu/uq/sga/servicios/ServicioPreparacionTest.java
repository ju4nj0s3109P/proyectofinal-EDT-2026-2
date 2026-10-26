package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.dominio.contratos.CalculadorRutas;
import co.edu.uq.sga.dominio.contratos.ColaPreparaciones;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.enums.EstadoTareaPreparacion;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.TareaPreparacion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ServicioPreparacionTest {
    @Test
    void encolaDespachaEnFifoYCompletaLasTareas() {
        ColaFalsa cola = new ColaFalsa();
        ServicioPreparacion servicio = new ServicioPreparacion(cola, new RutasFalsas());
        Apartamento apt1 = crearApartamento("A-1");
        Apartamento apt2 = crearApartamento("A-2");
        TareaPreparacion tarea1 = crearTarea("T-1", apt1);
        TareaPreparacion tarea2 = crearTarea("T-2", apt2);

        servicio.encolar(tarea1);
        servicio.encolar(tarea2);
        assertEquals(EstadoApartamento.EN_PREPARACION, apt1.getEstadoOperativo());
        assertThrows(IllegalStateException.class, () -> servicio.encolar(tarea1));

        assertEquals(tarea1, servicio.despacharSiguiente().orElseThrow());
        assertEquals(EstadoTareaPreparacion.EN_PROCESO, tarea1.getEstado());
        assertThrows(IllegalStateException.class, servicio::despacharSiguiente);
        servicio.completar(tarea1);
        assertEquals(EstadoTareaPreparacion.COMPLETADA, tarea1.getEstado());
        assertEquals(EstadoApartamento.PREPARADO, apt1.getEstadoOperativo());

        assertEquals(tarea2, servicio.despacharSiguiente().orElseThrow());
        servicio.completar(tarea2);
        assertTrue(servicio.despacharSiguiente().isEmpty());
    }

    @Test
    void noPermiteCompletarTareaAjenaAlServicio() {
        ServicioPreparacion servicio = new ServicioPreparacion(new ColaFalsa(), new RutasFalsas());
        assertThrows(IllegalStateException.class,
                () -> servicio.completar(crearTarea("T-3", crearApartamento("A-3"))));
    }

    @Test
    void calculaRutaYDevuelveUnaCopia() {
        RutasFalsas rutas = new RutasFalsas();
        ServicioPreparacion servicio = new ServicioPreparacion(new ColaFalsa(), rutas);
        String[] ruta = servicio.calcularRuta("recepcion", "A-1");
        assertArrayEquals(new String[] {"recepcion", "pasillo", "A-1"}, ruta);
        ruta[0] = "modificada";
        assertArrayEquals(new String[] {"recepcion", "pasillo", "A-1"},
                servicio.calcularRuta("recepcion", "A-1"));

        rutas.sinRuta = true;
        assertThrows(IllegalStateException.class,
                () -> servicio.calcularRuta("recepcion", "A-1"));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.calcularRuta(" ", "A-1"));
    }

    private TareaPreparacion crearTarea(String codigo, Apartamento apartamento) {
        return new TareaPreparacion(codigo, apartamento, LocalDateTime.of(2026, 10, 26, 10, 0));
    }

    private Apartamento crearApartamento(String codigo) {
        Apartamento apartamento = new Apartamento(codigo, "Suite", "A", 1, 1, 2,
                new String[0], BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        apartamento.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);
        return apartamento;
    }

    private static final class ColaFalsa implements ColaPreparaciones {
        private TareaPreparacion[] tareas = new TareaPreparacion[0];

        @Override
        public void encolar(TareaPreparacion tarea) {
            TareaPreparacion[] nuevas = new TareaPreparacion[tareas.length + 1];
            System.arraycopy(tareas, 0, nuevas, 0, tareas.length);
            nuevas[tareas.length] = tarea;
            tareas = nuevas;
        }

        @Override
        public boolean estaVacia() {
            return tareas.length == 0;
        }

        @Override
        public TareaPreparacion desencolar() {
            if (estaVacia()) {
                throw new IllegalStateException("La cola está vacía.");
            }
            TareaPreparacion primera = tareas[0];
            TareaPreparacion[] restantes = new TareaPreparacion[tareas.length - 1];
            System.arraycopy(tareas, 1, restantes, 0, restantes.length);
            tareas = restantes;
            return primera;
        }
    }

    private static final class RutasFalsas implements CalculadorRutas {
        private boolean sinRuta;

        @Override
        public String[] calcularRuta(String origen, String destino) {
            return sinRuta ? new String[0] : new String[] {origen, "pasillo", destino};
        }
    }
}
