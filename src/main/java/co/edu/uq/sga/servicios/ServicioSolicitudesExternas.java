package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.contratos.ColaSolicitudesExternas;
import co.edu.uq.sga.dominio.contratos.RegistroConflictosReservas;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.excepciones.ExcepcionDominio;
import co.edu.uq.sga.dominio.modelo.AsignacionApartamento;
import co.edu.uq.sga.dominio.modelo.ConflictoReserva;
import co.edu.uq.sga.dominio.modelo.Reserva;
import java.time.LocalDateTime;
import java.util.Optional;

/** Procesa solicitudes externas en FIFO y registra las rechazadas. */
public class ServicioSolicitudesExternas {
    private static final String MOTIVO_SOLAPAMIENTO = "conflicto de disponibilidad";
    private static final String MOTIVO_FUERA_SERVICIO = "apartamento fuera de servicio";

    private final ServicioReservas servicioReservas;
    private final ColaSolicitudesExternas cola;
    private final RegistroConflictosReservas registroConflictos;

    public ServicioSolicitudesExternas(
            ServicioReservas servicioReservas,
            ColaSolicitudesExternas cola,
            RegistroConflictosReservas registroConflictos) {
        if (servicioReservas == null || cola == null || registroConflictos == null) {
            throw new IllegalArgumentException(
                    "El servicio de reservas, la cola y el registro de conflictos son obligatorios.");
        }
        this.servicioReservas = servicioReservas;
        this.cola = cola;
        this.registroConflictos = registroConflictos;
    }

    /** Añade una solicitud externa pendiente para procesarla después. */
    public void encolar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria.");
        }
        if (reserva.getCanal() != CanalReserva.EXTERNA) {
            throw new IllegalArgumentException("La solicitud debe tener canal externo.");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ExcepcionDominio("La solicitud externa debe estar pendiente.");
        }
        if (cola.contieneFolio(reserva.getFolio())) {
            throw new ExcepcionDominio("La solicitud ya está encolada.");
        }
        cola.encolar(reserva);
    }

    /** Procesa una solicitud; devuelve vacío si la cola no tiene solicitudes. */
    public Optional<Resultado> procesarSiguiente(LocalDateTime fechaHoraProceso) {
        if (fechaHoraProceso == null) {
            throw new IllegalArgumentException("La fecha y hora de proceso son obligatorias.");
        }
        if (cola.estaVacia()) {
            return Optional.empty();
        }
        Reserva reserva = cola.desencolar();
        if (reserva == null || reserva.getCanal() != CanalReserva.EXTERNA
                || reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ExcepcionDominio("La cola contiene una solicitud externa inválida.");
        }

        String motivo = obtenerMotivoOperativo(reserva);
        if (motivo == null && servicioReservas.aceptarExterna(reserva)) {
            return Optional.of(new Resultado(reserva, true, null));
        }
        if (motivo == null) {
            motivo = MOTIVO_SOLAPAMIENTO;
        }
        ConflictoReserva conflicto = new ConflictoReserva(reserva, motivo, fechaHoraProceso);
        registroConflictos.registrar(conflicto);
        reserva.rechazar();
        return Optional.of(new Resultado(reserva, false, conflicto));
    }

    private String obtenerMotivoOperativo(Reserva reserva) {
        for (AsignacionApartamento asignacion : reserva.getAsignaciones()) {
            if (asignacion.getApartamento().getEstadoOperativo()
                    == EstadoApartamento.FUERA_DE_SERVICIO) {
                return MOTIVO_FUERA_SERVICIO;
            }
        }
        return null;
    }

    /** Resultado de procesar una solicitud externa. */
    public record Resultado(Reserva reserva, boolean aceptada, ConflictoReserva conflicto) {
        public Resultado {
            if (reserva == null) {
                throw new IllegalArgumentException("La reserva del resultado es obligatoria.");
            }
            if (aceptada && conflicto != null || !aceptada && conflicto == null) {
                throw new IllegalArgumentException(
                        "El resultado debe concordar con la presencia de un conflicto.");
            }
        }
    }
}
