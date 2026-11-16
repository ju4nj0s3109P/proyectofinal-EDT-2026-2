package co.edu.uq.sga.dominio.contratos;

import co.edu.uq.sga.dominio.modelo.Reserva;

/**
 * Puerto de las reservas pendientes ordenadas por su hora de vencimiento,
 * implementado con la cola de prioridad propia.
 */
public interface ColaReservasPendientes {

    void encolar(Reserva reserva);

    boolean estaVacia();

    /** La reserva que vence primero, sin retirarla; requiere una cola no vacía. */
    Reserva verProxima();

    /** Retira la reserva que vence primero; requiere una cola no vacía. */
    Reserva desencolar();

    /**
     * Saca una reserva que dejó de estar pendiente porque se confirmó o se canceló.
     *
     * @return true si estaba en la cola
     */
    boolean retirar(Reserva reserva);
}
