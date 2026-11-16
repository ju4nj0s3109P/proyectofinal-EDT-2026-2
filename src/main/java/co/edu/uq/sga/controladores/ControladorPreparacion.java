package co.edu.uq.sga.controladores;

import co.edu.uq.sga.servicios.ServicioPanelPreparacion;
import co.edu.uq.sga.servicios.ServicioPanelPreparacion.ResumenTarea;

import java.util.Objects;
import java.util.Optional;

public final class ControladorPreparacion {

    public interface VistaPreparacion {
        void mostrar(
                ResumenTarea[] pendientes,
                Optional<ResumenTarea> actual
        );

        void mostrarError(String mensaje);
    }

    private final ServicioPanelPreparacion servicio;
    private final VistaPreparacion vista;

    public ControladorPreparacion(
            ServicioPanelPreparacion servicio,
            VistaPreparacion vista
    ) {
        this.servicio = Objects.requireNonNull(servicio);
        this.vista = Objects.requireNonNull(vista);
    }

    public void actualizar() {
        vista.mostrar(
                servicio.consultarPendientes(),
                servicio.consultarActual()
        );
    }

    public void despachar() {
        try {
            servicio.despacharSiguiente();
        } catch (IllegalStateException | IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        }

        actualizar();
    }

    public void completar() {
        try {
            servicio.completarActual();
        } catch (IllegalStateException | IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        }

        actualizar();
    }
}