package co.edu.uq.sga.dominio.contratos;

import co.edu.uq.sga.dominio.modelo.TareaPreparacion;

/** Puerto de preparación; la política de extracción la define el adaptador de estructuras. */
public interface ColaPreparaciones {
    void encolar(TareaPreparacion tarea);
    boolean estaVacia();

    /** Retira la siguiente tarea según la política configurada; requiere una cola no vacía. */
    TareaPreparacion desencolar();
}
