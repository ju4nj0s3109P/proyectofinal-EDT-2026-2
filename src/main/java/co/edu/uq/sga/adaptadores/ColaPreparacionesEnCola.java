package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.contratos.ColaPreparaciones;
import co.edu.uq.sga.dominio.modelo.TareaPreparacion;
import co.edu.uq.sga.estructuras.lineales.Cola;
import java.util.Iterator;

public class ColaPreparacionesEnCola implements ColaPreparaciones, Iterable<TareaPreparacion> {

    private final Cola<TareaPreparacion> cola = new Cola<>();

    @Override
    public void encolar(TareaPreparacion tarea) {
        if (tarea == null) {
            throw new IllegalArgumentException("La tarea es obligatoria.");
        }
        cola.encolar(tarea);
    }

    @Override
    public boolean estaVacia() {
        return cola.estaVacia();
    }

    @Override
    public TareaPreparacion desencolar() {
        return cola.desencolar();
    }

    public int tamanio() {
        return cola.tamanio();
    }

    @Override
    public Iterator<TareaPreparacion> iterator() {
        return cola.iterator();
    }
}
