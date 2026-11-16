package co.edu.uq.sga.controladores;

import co.edu.uq.sga.servicios.ServicioApartamentos;
import co.edu.uq.sga.servicios.ServicioApartamentos.ResumenApartamento;

import java.util.Objects;

public final class ControladorTablero {

    public interface VistaTablero {
        void mostrarApartamentos(ResumenApartamento[] apartamentos);
    }

    private final ServicioApartamentos servicio;
    private final VistaTablero vista;

    public ControladorTablero(
            ServicioApartamentos servicio,
            VistaTablero vista
    ) {
        this.servicio = Objects.requireNonNull(
                servicio,
                "El servicio de apartamentos es obligatorio."
        );

        this.vista = Objects.requireNonNull(
                vista,
                "La vista del tablero es obligatoria."
        );
    }

    public void actualizar() {
        vista.mostrarApartamentos(servicio.consultarTodos());
    }
}