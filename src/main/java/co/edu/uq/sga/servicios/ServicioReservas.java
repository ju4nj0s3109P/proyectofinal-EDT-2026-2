package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.contratos.IndiceReservas;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.excepciones.AnticipoInsuficienteException;
import co.edu.uq.sga.dominio.excepciones.ConflictoDisponibilidadException;
import co.edu.uq.sga.dominio.excepciones.ExcepcionDominio;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.AsignacionApartamento;
import co.edu.uq.sga.dominio.modelo.Folio;
import co.edu.uq.sga.dominio.modelo.Reserva;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Coordina el ciclo de vida de una reserva con el índice y el folio. */
public class ServicioReservas {
    private static final BigDecimal PORCENTAJE_ANTICIPO = new BigDecimal("0.30");
    private static final int HORAS_PARA_VENCER = 24;

    /** Momento en que vence una reserva pendiente del portal. */
    public static LocalDateTime vencimientoDe(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria.");
        }
        return reserva.getFechaHoraCreacion().plusHours(HORAS_PARA_VENCER);
    }

    private final IndiceReservas indice;

    public ServicioReservas(IndiceReservas indice) {
        if (indice == null) {
            throw new IllegalArgumentException("El índice de reservas es obligatorio.");
        }
        this.indice = indice;
    }

    /**
     * Consulta disponibilidad sin registrar ni modificar la reserva.
     * Los apartamentos fuera de servicio no se ofrecen aunque el intervalo esté libre.
     */
    public boolean consultarDisponibilidad(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria.");
        }
        for (AsignacionApartamento asignacion : reserva.getAsignaciones()) {
            if (asignacion.getApartamento().getEstadoOperativo()
                    == EstadoApartamento.FUERA_DE_SERVICIO) {
                return false;
            }
        }
        return indice.estaDisponible(reserva);
    }

    /** Registra desde el portal y mantiene la reserva pendiente. */
    public void registrarPortal(Reserva reserva) {
        validarNuevaReserva(reserva, CanalReserva.PORTAL);
        agregarSiDisponible(reserva);
    }

    /** Registra una reserva de recepción y la confirma si hay disponibilidad. */
    public void registrarDirecta(Reserva reserva) {
        validarNuevaReserva(reserva, CanalReserva.DIRECTA);
        agregarSiDisponible(reserva);
        reserva.confirmar();
    }

    /**
     * Procesa una solicitud externa ya retirada de la cola FIFO.
     *
     * @return false ante conflicto, para que el procesador de cola la rechace y
     *     registre el conflicto
     */
    public boolean aceptarExterna(Reserva reserva) {
        validarNuevaReserva(reserva, CanalReserva.EXTERNA);
        if (!indice.agregarSiDisponible(reserva)) {
            return false;
        }
        reserva.confirmar();
        return true;
    }

    /** Confirma una reserva del portal al alcanzar el anticipo mínimo del 30 %. */
    public void confirmarPortal(Reserva reserva, Folio folio, BigDecimal totalCotizacion) {
        validarReservaRegistrada(reserva, CanalReserva.PORTAL, EstadoReserva.PENDIENTE);
        validarFolio(reserva, folio);
        if (totalCotizacion == null || totalCotizacion.signum() < 0) {
            throw new IllegalArgumentException("La cotización debe ser no negativa.");
        }

        BigDecimal anticipo = ServicioFolios.totalPagado(folio);
        BigDecimal minimo = totalCotizacion.multiply(PORCENTAJE_ANTICIPO);
        if (anticipo.compareTo(minimo) < 0) {
            throw new AnticipoInsuficienteException("El anticipo no alcanza el 30 % de la cotización.");
        }
        reserva.confirmar();
    }

    /** Dinero que se le devolvería al huésped si cancela ahora. */
    public BigDecimal calcularDevolucion(Folio folio) {
        return ServicioFolios.totalPagado(folio);
    }

    /**
     * Cancela antes del check-in. El dinero recibido debe estar devuelto antes,
     * con {@link ServicioFolios#cerrarPorCancelacion}: el folio es el único que
     * registra movimientos, así que aquí solo se comprueba.
     */
    public void cancelar(Reserva reserva, Folio folio) {
        validarReservaRegistrada(reserva, null, null);
        if (reserva.getEstado() != EstadoReserva.PENDIENTE
                && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new ExcepcionDominio("Solo se puede cancelar antes del check-in.");
        }
        validarFolio(reserva, folio);
        BigDecimal pendienteDeDevolver = calcularDevolucion(folio);
        if (pendienteDeDevolver.signum() != 0) {
            throw new ExcepcionDominio(
                    "Antes de cancelar hay que devolver los " + pendienteDeDevolver
                            + " recibidos del huésped.");
        }

        retirarDelIndice(reserva);
        reserva.cancelar();
    }

    /**
     * Expira una reserva del portal cuando sigue pendiente al cumplir 24 horas.
     *
     * @return true si venció y se liberó; false si todavía no venció
     */
    public boolean expirarPendiente(Reserva reserva, LocalDateTime ahora) {
        if (reserva == null) throw new IllegalArgumentException("La reserva es obligatoria.");
        if (ahora == null) throw new IllegalArgumentException("La fecha y hora actual son obligatorias.");
        if (reserva.getCanal() != CanalReserva.PORTAL) {
            throw new IllegalArgumentException("La expiración automática solo aplica a reservas del portal.");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ExcepcionDominio("Solo puede expirar una reserva pendiente.");
        }
        if (ahora.isBefore(vencimientoDe(reserva))) return false;
        validarReservaRegistrada(reserva, CanalReserva.PORTAL, EstadoReserva.PENDIENTE);
        retirarDelIndice(reserva);
        reserva.cancelar();
        return true;
    }

    /** Marca como no-show una reserva confirmada y libera todas sus asignaciones. */
    public void marcarNoShow(Reserva reserva) {
        validarReservaRegistrada(reserva, null, EstadoReserva.CONFIRMADA);
        retirarDelIndice(reserva);
        reserva.marcarNoShow();
    }

    /** Inicia la estancia solo si todos los apartamentos están preparados. */
    public void registrarCheckIn(Reserva reserva) {
        validarReservaRegistrada(reserva, null, EstadoReserva.CONFIRMADA);
        Apartamento[] apartamentos = apartamentosDe(reserva);
        for (Apartamento apartamento : apartamentos) {
            if (apartamento.getEstadoOperativo() != EstadoApartamento.PREPARADO) {
                throw new ExcepcionDominio(
                        "Todos los apartamentos asignados deben estar preparados.");
            }
        }
        reserva.iniciar();
        for (Apartamento apartamento : apartamentos) {
            apartamento.cambiarEstadoOperativo(EstadoApartamento.OCUPADO);
        }
    }

    /** Finaliza la estancia y libera los apartamentos para preparación. */
    public void registrarCheckOut(Reserva reserva) {
        validarReservaRegistrada(reserva, null, EstadoReserva.EN_CURSO);
        Apartamento[] apartamentos = apartamentosDe(reserva);
        for (Apartamento apartamento : apartamentos) {
            if (apartamento.getEstadoOperativo() != EstadoApartamento.OCUPADO) {
                throw new ExcepcionDominio(
                        "Todos los apartamentos asignados deben estar ocupados.");
            }
        }
        retirarDelIndice(reserva);
        reserva.finalizar();
        for (Apartamento apartamento : apartamentos) {
            apartamento.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);
        }
    }

    private void validarNuevaReserva(Reserva reserva, CanalReserva canalEsperado) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria.");
        }
        if (reserva.getCanal() != canalEsperado) {
            throw new IllegalArgumentException("El canal no corresponde a esta operación.");
        }
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ExcepcionDominio("La reserva nueva debe estar pendiente.");
        }
        validarApartamentosDisponiblesOperativamente(reserva);
    }

    private void validarApartamentosDisponiblesOperativamente(Reserva reserva) {
        for (AsignacionApartamento asignacion : reserva.getAsignaciones()) {
            if (asignacion.getApartamento().getEstadoOperativo()
                    == EstadoApartamento.FUERA_DE_SERVICIO) {
                throw new ExcepcionDominio(
                        "No se puede reservar un apartamento fuera de servicio.");
            }
        }
    }

    private void validarReservaRegistrada(
            Reserva reserva, CanalReserva canal, EstadoReserva estado) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria.");
        }
        if (canal != null && reserva.getCanal() != canal) {
            throw new IllegalArgumentException("El canal no corresponde a esta operación.");
        }
        if (estado != null && reserva.getEstado() != estado) {
            throw new ExcepcionDominio("La reserva no está en el estado requerido.");
        }
        if (!indice.contiene(reserva)) {
            throw new ExcepcionDominio("La reserva no está registrada en el índice activo.");
        }
    }

    private void validarFolio(Reserva reserva, Folio folio) {
        if (folio == null) {
            throw new IllegalArgumentException("El folio es obligatorio.");
        }
        if (!reserva.getFolio().equals(folio.getCodigo())) {
            throw new IllegalArgumentException("El folio no corresponde a la reserva.");
        }
    }

    private void agregarSiDisponible(Reserva reserva) {
        if (!indice.agregarSiDisponible(reserva)) {
            throw new ConflictoDisponibilidadException("Hay un conflicto de disponibilidad.");
        }
    }

    private void retirarDelIndice(Reserva reserva) {
        if (!indice.retirarSiExiste(reserva)) {
            throw new ExcepcionDominio("No fue posible retirar la reserva del índice.");
        }
    }

    private static Apartamento[] apartamentosDe(Reserva reserva) {
        AsignacionApartamento[] asignaciones = reserva.getAsignaciones();
        Apartamento[] apartamentos = new Apartamento[asignaciones.length];
        for (int i = 0; i < asignaciones.length; i++) {
            apartamentos[i] = asignaciones[i].getApartamento();
        }
        return apartamentos;
    }
}
