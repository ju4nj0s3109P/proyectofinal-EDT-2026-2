package co.edu.uq.sga.controladores;

import co.edu.uq.sga.servicios.ServicioPanelReservas;
import co.edu.uq.sga.servicios.ServicioPanelReservas.DatosOcupante;
import co.edu.uq.sga.servicios.ServicioPanelReservas.ResumenReserva;
import co.edu.uq.sga.servicios.ServicioPanelReservas.ApartamentoDisponible;
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

    public void cancelarReserva(String folio) {
        try {
            servicio.cancelarReserva(folio);
            actualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    public void marcarNoShow(String folio) {
        try {
            servicio.marcarNoShow(folio);
            actualizar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
        }
    }
    public Optional<ServicioPanelReservas.CotizacionReserva> cotizar(String folio) {
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

    public boolean registrarExterna(
            String folio, String documento, String[] apartamentos, DatosOcupante[] ocupantes,
            LocalDate entrada, LocalDate salida, LocalTime hora) {
        try {
            servicio.registrar(folio, documento, apartamentos, ocupantes, entrada, salida, hora,
                    co.edu.uq.sga.dominio.enums.CanalReserva.EXTERNA);
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
            return false;
        }
        actualizar();
        return true;
    }

    public boolean registrarPortal(
            String folio,
            String documento,
            String[] apartamentos,
            DatosOcupante[] ocupantes,
            LocalDate entrada,
            LocalDate salida,
            LocalTime hora
    ) {
        try {
            servicio.registrarPortal(
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

    public void confirmarPortal(String folio) {
        try {
            servicio.confirmarPortal(folio);
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
        }

        actualizar();
    }

    public Optional<ServicioPanelReservas.CotizacionReserva> cotizarBorrador(
            String[] apartamentos,
            DatosOcupante[] ocupantes,
            LocalDate entrada,
            LocalDate salida
    ) {
        try {
            return Optional.of(servicio.cotizarBorrador(
                    apartamentos, ocupantes, entrada, salida
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ApartamentoDisponible[]> buscarDisponibles(
            LocalDate entrada,
            LocalDate salida,
            int personas,
            String orden
    ) {
        try {
            return Optional.of(servicio.buscarDisponibles(
                    entrada, salida, personas, orden
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ApartamentoDisponible[]> proponerGrupo(
            LocalDate entrada,
            LocalDate salida,
            int personas
    ) {
        try {
            return Optional.of(servicio.proponerGrupo(
                    entrada, salida, personas
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            vista.mostrarError(e.getMessage());
            return Optional.empty();
        }
    }
}