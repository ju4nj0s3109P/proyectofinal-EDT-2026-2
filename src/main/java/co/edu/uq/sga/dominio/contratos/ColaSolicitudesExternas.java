package co.edu.uq.sga.dominio.contratos;

import co.edu.uq.sga.dominio.modelo.Reserva;

/** Cola FIFO de solicitudes externas, implementada con una estructura propia. */
public interface ColaSolicitudesExternas {
    void encolar(Reserva reserva);
    boolean estaVacia();

    /** Evita encolar dos veces una solicitud que ya espera procesamiento. */
    boolean contieneFolio(String folio);

    /** Retira la solicitud más antigua; requiere una cola no vacía. */
    Reserva desencolar();
}
