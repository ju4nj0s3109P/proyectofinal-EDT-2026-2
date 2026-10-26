package co.edu.uq.sga.dominio.contratos;

import co.edu.uq.sga.dominio.modelo.TareaPreparacion;

/** Puerto FIFO para conectar el servicio con la cola de estructuras. */
public interface ColaPreparaciones {
    void encolar(TareaPreparacion tarea);
    boolean estaVacia();

    /** Retira la tarea más antigua; requiere una cola no vacía. */
    TareaPreparacion desencolar();
}
