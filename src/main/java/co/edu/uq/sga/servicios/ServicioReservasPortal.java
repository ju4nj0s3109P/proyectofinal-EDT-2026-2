package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.modelo.Folio;
import co.edu.uq.sga.dominio.modelo.Reserva;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Orquesta el registro, confirmación y vigilancia de las reservas del portal. */
public final class ServicioReservasPortal {
    private final ServicioReservas reservas;
    private final ServicioVencimientoReservas vencimientos;

    public ServicioReservasPortal(ServicioReservas reservas, ServicioVencimientoReservas vencimientos) {
        this.reservas = Objects.requireNonNull(reservas, "El servicio de reservas es obligatorio.");
        this.vencimientos = Objects.requireNonNull(vencimientos, "El servicio de vencimientos es obligatorio.");
    }

    /** Registra primero el bloqueo de noches y luego incorpora la reserva a la cola de vencimiento. */
    public void registrarPendiente(Reserva reserva) {
        reservas.registrarPortal(reserva);
        vencimientos.vigilar(reserva);
    }

    /** Confirma con el anticipo y deja de vigilar la reserva cuando la operación tiene éxito. */
    public void confirmar(Reserva reserva, Folio folio, BigDecimal cotizacion) {
        reservas.confirmarPortal(reserva, folio, cotizacion);
        vencimientos.dejarDeVigilar(reserva);
    }

    public int procesarVencidas(LocalDateTime ahora) {
        return vencimientos.procesarVencidas(ahora);
    }

    public boolean tienePendientesPorVencer() {
        return vencimientos.hayPendientes();
    }
}
