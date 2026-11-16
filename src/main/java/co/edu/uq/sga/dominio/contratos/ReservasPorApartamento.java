package co.edu.uq.sga.dominio.contratos;

import co.edu.uq.sga.dominio.modelo.Reserva;

/**
 * Puerto de consulta de las reservas activas de un apartamento. Lo usa la
 * búsqueda de disponibilidad para no recorrer todas las reservas del alojamiento.
 */
public interface ReservasPorApartamento {

    /** Reservas activas del apartamento, ordenadas por fecha de entrada; vacío si no tiene. */
    Reserva[] de(String codigoApartamento);
}
