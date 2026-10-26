package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.contratos.IndiceReservas;
import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.enums.EstadoReserva;
import co.edu.uq.sga.dominio.enums.TipoMovimiento;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Folio;
import co.edu.uq.sga.dominio.modelo.MovimientoFolio;
import co.edu.uq.sga.dominio.modelo.Reserva;
import java.math.BigDecimal;

/** Coordina el ciclo de vida de una reserva con el índice y el folio. */
public class ServicioReservas {
    private static final BigDecimal PORCENTAJE_ANTICIPO = new BigDecimal("0.30");

    private final IndiceReservas indice;

    public ServicioReservas(IndiceReservas indice) {
        if (indice == null) {
            throw new IllegalArgumentException("El índice de reservas es obligatorio.");
        }
        this.indice = indice;
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

        BigDecimal anticipo = folio.calcularSaldo().negate().max(BigDecimal.ZERO);
        BigDecimal minimo = totalCotizacion.multiply(PORCENTAJE_ANTICIPO);
        if (anticipo.compareTo(minimo) < 0) {
            throw new IllegalStateException("El anticipo no alcanza el 30 % de la cotización.");
        }
        reserva.confirmar();
    }

    /** Calcula el saldo a favor que se devolvería al cancelar. */
    public BigDecimal calcularDevolucion(Folio folio) {
        if (folio == null) {
            throw new IllegalArgumentException("El folio es obligatorio.");
        }
        return folio.calcularSaldo().negate().max(BigDecimal.ZERO);
    }

    /**
     * Cancela antes del check-in. Si hay saldo a favor exige registrar exactamente
     * una devolución; las devoluciones no borran los pagos originales.
     */
    public void cancelar(Reserva reserva, Folio folio, MovimientoFolio devolucion) {
        validarReservaRegistrada(reserva, null, null);
        if (reserva.getEstado() != EstadoReserva.PENDIENTE
                && reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Solo se puede cancelar antes del check-in.");
        }
        validarFolio(reserva, folio);

        BigDecimal valorDevolucion = calcularDevolucion(folio);
        if (valorDevolucion.signum() == 0) {
            if (devolucion != null) {
                throw new IllegalArgumentException("No se debe registrar una devolución por cero.");
            }
        } else if (devolucion == null
                || devolucion.getTipo() != TipoMovimiento.DEVOLUCION
                || devolucion.getMonto().compareTo(valorDevolucion) != 0
                || devolucion.getCodigoMovimientoReversado() != null) {
            throw new IllegalArgumentException(
                    "La devolución debe corresponder al saldo a favor completo.");
        }

        if (devolucion != null) {
            folio.registrarMovimiento(devolucion);
        }
        retirarDelIndice(reserva);
        reserva.cancelar();
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
        Asignaciones asignaciones = obtenerAsignaciones(reserva);
        for (Apartamento apartamento : asignaciones.apartamentos()) {
            if (apartamento.getEstadoOperativo() != EstadoApartamento.PREPARADO) {
                throw new IllegalStateException(
                        "Todos los apartamentos asignados deben estar preparados.");
            }
        }
        reserva.iniciar();
        for (Apartamento apartamento : asignaciones.apartamentos()) {
            apartamento.cambiarEstadoOperativo(EstadoApartamento.OCUPADO);
        }
    }

    /** Finaliza la estancia y libera los apartamentos para preparación. */
    public void registrarCheckOut(Reserva reserva) {
        validarReservaRegistrada(reserva, null, EstadoReserva.EN_CURSO);
        Asignaciones asignaciones = obtenerAsignaciones(reserva);
        for (Apartamento apartamento : asignaciones.apartamentos()) {
            if (apartamento.getEstadoOperativo() != EstadoApartamento.OCUPADO) {
                throw new IllegalStateException(
                        "Todos los apartamentos asignados deben estar ocupados.");
            }
        }
        retirarDelIndice(reserva);
        reserva.finalizar();
        for (Apartamento apartamento : asignaciones.apartamentos()) {
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
            throw new IllegalStateException("La reserva nueva debe estar pendiente.");
        }
        validarApartamentosDisponiblesOperativamente(reserva);
    }

    private void validarApartamentosDisponiblesOperativamente(Reserva reserva) {
        for (co.edu.uq.sga.dominio.modelo.AsignacionApartamento asignacion
                : reserva.getAsignaciones()) {
            if (asignacion.getApartamento().getEstadoOperativo()
                    == EstadoApartamento.FUERA_DE_SERVICIO) {
                throw new IllegalStateException(
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
            throw new IllegalStateException("La reserva no está en el estado requerido.");
        }
        if (!indice.contiene(reserva)) {
            throw new IllegalStateException("La reserva no está registrada en el índice activo.");
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
            throw new IllegalStateException("Hay un conflicto de disponibilidad.");
        }
    }

    private void retirarDelIndice(Reserva reserva) {
        if (!indice.retirarSiExiste(reserva)) {
            throw new IllegalStateException("No fue posible retirar la reserva del índice.");
        }
    }

    private Asignaciones obtenerAsignaciones(Reserva reserva) {
        co.edu.uq.sga.dominio.modelo.AsignacionApartamento[] asignaciones =
                reserva.getAsignaciones();
        Apartamento[] apartamentos = new Apartamento[asignaciones.length];
        for (int i = 0; i < asignaciones.length; i++) {
            apartamentos[i] = asignaciones[i].getApartamento();
        }
        return new Asignaciones(apartamentos);
    }

    private record Asignaciones(Apartamento[] apartamentos) {}
}
