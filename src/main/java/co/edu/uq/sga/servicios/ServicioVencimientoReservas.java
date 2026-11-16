package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.contratos.ColaReservasPendientes;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.excepciones.ExcepcionDominio;
import co.edu.uq.sga.dominio.modelo.Reserva;
import java.time.LocalDateTime;
import java.util.Comparator;

/**
 * Vencimiento automático de las reservas del portal: las pendientes esperan en una
 * cola de prioridad por hora de vencimiento y, al pasar el plazo, se cancelan y
 * liberan sus noches.
 */
public class ServicioVencimientoReservas {

    private final ServicioReservas servicioReservas;
    private final ColaReservasPendientes cola;

    public ServicioVencimientoReservas(
            ServicioReservas servicioReservas, ColaReservasPendientes cola) {
        if (servicioReservas == null || cola == null) {
            throw new IllegalArgumentException(
                    "El servicio de reservas y la cola de pendientes son obligatorios.");
        }
        this.servicioReservas = servicioReservas;
        this.cola = cola;
    }

    /** Criterio de la cola: vence antes primero, y a igual hora manda el folio. */
    public static Comparator<Reserva> comparadorPorVencimiento() {
        Comparator<Reserva> porVencimiento =
                Comparator.comparing(ServicioReservas::vencimientoDe);
        return porVencimiento.thenComparing(Reserva::getFolio);
    }

    /** Pone a vigilar una reserva del portal recién registrada. */
    public void vigilar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria.");
        }
        if (reserva.getCanal() != CanalReserva.PORTAL) {
            throw new IllegalArgumentException(
                    "El vencimiento automático solo aplica a reservas del portal.");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ExcepcionDominio("Solo se vigila una reserva pendiente.");
        }
        cola.encolar(reserva);
    }

    /** Deja de vigilar una reserva que ya se confirmó o se canceló. */
    public boolean dejarDeVigilar(Reserva reserva) {
        return cola.retirar(reserva);
    }

    /**
     * Cancela todas las pendientes cuyo plazo ya pasó. La cabeza de la cola es la
     * que vence primero, así que al encontrar una vigente se detiene.
     *
     * @return cuántas reservas venció
     */
    public int procesarVencidas(LocalDateTime ahora) {
        if (ahora == null) {
            throw new IllegalArgumentException("La fecha y hora actual son obligatorias.");
        }
        int vencidas = 0;
        while (!cola.estaVacia()
                && !ahora.isBefore(ServicioReservas.vencimientoDe(cola.verProxima()))) {
            Reserva reserva = cola.desencolar();
            // Si dejó de estar pendiente mientras esperaba, solo sale de la cola.
            if (reserva.getEstado() == EstadoReserva.PENDIENTE
                    && servicioReservas.expirarPendiente(reserva, ahora)) {
                vencidas++;
            }
        }
        return vencidas;
    }

    /** Indica si queda alguna reserva pendiente en vigilancia. */
    public boolean hayPendientes() {
        return !cola.estaVacia();
    }
}
