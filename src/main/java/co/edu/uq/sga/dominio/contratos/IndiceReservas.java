package co.edu.uq.sga.dominio.contratos;

import co.edu.uq.sga.dominio.modelo.Reserva;

/**
 * Puerto del índice activo de reservas. Su implementación debe usar un árbol
 * de búsqueda por apartamento y fecha.
 */
public interface IndiceReservas {

    /**
     * Registra todas las asignaciones de forma atómica si no se solapan.
     *
     * @return true si registró todas las asignaciones; false si existe conflicto
     */
    boolean agregarSiDisponible(Reserva reserva);

    /**
     * Retira de forma atómica todas las asignaciones activas de la reserva.
     *
     * @return true si las retiró; false si la reserva no estaba indexada
     */
    boolean retirarSiExiste(Reserva reserva);

    /** Indica si todas las asignaciones de la reserva están activas en el índice. */
    boolean contiene(Reserva reserva);
}
