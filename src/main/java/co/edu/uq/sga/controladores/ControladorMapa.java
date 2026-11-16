package co.edu.uq.sga.controladores;

import co.edu.uq.sga.servicios.ServicioMapa;
import co.edu.uq.sga.servicios.ServicioMapa.Pasillo;
import co.edu.uq.sga.servicios.ServicioMapa.Recorrido;
import co.edu.uq.sga.servicios.ServicioMapa.Ubicacion;

import java.util.Objects;

public final class ControladorMapa {

    public interface VistaMapa {
        void mostrarRecorrido(String destino, Recorrido recorrido);
    }

    private final ServicioMapa servicio;
    private final VistaMapa vista;

    public ControladorMapa(ServicioMapa servicio, VistaMapa vista) {
        this.servicio = Objects.requireNonNull(
                servicio,
                "El servicio del mapa es obligatorio."
        );

        this.vista = Objects.requireNonNull(
                vista,
                "La vista del mapa es obligatoria."
        );
    }

    public Ubicacion[] consultarUbicaciones() {
        return servicio.consultarUbicaciones();
    }

    public Pasillo[] consultarPasillos() {
        return servicio.consultarPasillos();
    }

    public String consultarRecepcion() {
        return servicio.consultarRecepcion();
    }

    public void seleccionarDestino(String codigoDestino) {
        Recorrido recorrido = servicio.calcularDesdeRecepcion(codigoDestino);
        vista.mostrarRecorrido(codigoDestino, recorrido);
    }

    public boolean estaAbierto(Pasillo pasillo) {
        return servicio.estaAbierto(pasillo);
    }

    public void cambiarEstadoPasillo(Pasillo pasillo, boolean abierto) {
        servicio.cambiarEstadoPasillo(pasillo, abierto);
    }

    public Pasillo[] consultarPasillosCerrados() {
        return servicio.consultarPasillosCerrados();
    }

    public String[] consultarApartamentosInalcanzables() {
        return servicio.consultarApartamentosInalcanzables();
    }
}