package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.contratos.ColaPreparaciones;
import co.edu.uq.sga.dominio.modelo.TareaPreparacion;
import co.edu.uq.sga.estructuras.lineales.ColaPrioridad;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;

public class ColaPreparacionesPorPrioridad implements ColaPreparaciones, Iterable<TareaPreparacion> {

    private final ColaPrioridad<TareaPreparacion> cola;

    /** Crea la cola con la regla de negocio de llegadas del día. */
    public ColaPreparacionesPorPrioridad(LocalDate fechaActual) {
        this(TareaPreparacion.comparadorPorPrioridad(fechaActual));
    }

    public ColaPreparacionesPorPrioridad(Comparator<TareaPreparacion> prioridad) {
        if (prioridad == null) {
            throw new IllegalArgumentException("El criterio de prioridad es obligatorio.");
        }
        this.cola = new ColaPrioridad<>(prioridad);
    }

    @Override
    public void encolar(TareaPreparacion tarea) {
        if (tarea == null) {
            throw new IllegalArgumentException("La tarea es obligatoria.");
        }
        cola.insertar(tarea);
    }

    @Override
    public boolean estaVacia() {
        return cola.estaVacia();
    }

    @Override
    public TareaPreparacion desencolar() {
        return cola.extraerPrimero();
    }

    public int tamanio() {
        return cola.tamanio();
    }

    @Override
    public Iterator<TareaPreparacion> iterator() {
        return cola.iterator();
    }
}
