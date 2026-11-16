package co.edu.uq.sga.controladores;

import co.edu.uq.sga.servicios.ServicioPanelReservas;
import co.edu.uq.sga.servicios.ServicioPanelReservas.DatosOcupante;
import co.edu.uq.sga.servicios.ServicioPanelReservas.ResumenReserva;
import co.edu.uq.sga.servicios.ServicioTarifas;
import java.util.Optional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public final class ControladorReservas {

    public interface VistaReservas {
        void mostrarReservas(ResumenReserva[] reservas);
        void mostrarError(String mensaje);
    }

    private final ServicioPanelReservas servicio;
    private final VistaReservas vista;

    public ControladorReservas(
            ServicioPanelReservas servicio,
            VistaReservas vista
    ) {
        this.servicio = Objects.requireNonNull(servicio);
        this.vista = Objects.requireNonNull(vista);
    }

    public void actualizar() {
        vista.mostrarReservas(servicio.consultarTodas());
    }

    public void registrarCheckIn(String folio) {
        try {
            servicio.registrarCheckIn(folio);
            actualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    public void registrarCheckOut(String folio) {
        try {
            servicio.registrarCheckOut(folio);
            actualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    public Optional<ServicioTarifas.Cotizacion> cotizar(String folio) {
        try {
            return Optional.of(servicio.cotizarReserva(folio));
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
            return Optional.empty();
        }
    }

    public boolean registrar(
            String folio,
            String documento,
            String[] apartamentos,
            DatosOcupante[] ocupantes,
            LocalDate entrada,
            LocalDate salida,
            LocalTime hora
    ) {
        try {
            servicio.registrarDirecta(
                    folio, documento, apartamentos,
                    ocupantes, entrada, salida, hora
            );
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
            return false;
        }

        actualizar();
        return true;
    }
}