package co.edu.uq.sga.servicios;

import co.edu.uq.sga.adaptadores.CalculadorRutasGrafo;
import java.util.Objects;

/** Consultas del mapa para la interfaz; las rutas las resuelve el adaptador existente. */
public final class ServicioMapa {
    public record Ubicacion(String id, String tipo, String nombre) {
        @Override public String toString() { return nombre + " · " + id; }
    }

    public record Pasillo(String origen, String destino, double metros) {}

    public record Recorrido(String[] ubicaciones, double metros) {
        public Recorrido { ubicaciones = ubicaciones.clone(); }
        @Override public String[] ubicaciones() { return ubicaciones.clone(); }
        public boolean existe() { return ubicaciones.length > 0; }
    }

    private final Ubicacion[] ubicaciones;
    private final Pasillo[] pasillos;
    private final CalculadorRutasGrafo calculador;

    public ServicioMapa(Ubicacion[] ubicaciones, Pasillo[] pasillos,
                        CalculadorRutasGrafo calculador) {
        this.ubicaciones = Objects.requireNonNull(ubicaciones).clone();
        this.pasillos = Objects.requireNonNull(pasillos).clone();
        this.calculador = Objects.requireNonNull(calculador);
    }

    public Ubicacion[] consultarUbicaciones() { return ubicaciones.clone(); }
    public Pasillo[] consultarPasillos() { return pasillos.clone(); }

    public String consultarRecepcion() {
        for (Ubicacion ubicacion : ubicaciones) {
            if ("RECEPCION".equals(ubicacion.tipo())) {
                return ubicacion.id();
            }
        }
        throw new IllegalStateException("El mapa necesita una ubicación de tipo RECEPCION.");
    }

    public Recorrido calcularDesdeRecepcion(String destino) {
        String origen = consultarRecepcion();
        String[] ruta = calculador.calcularRuta(origen, destino);
        return new Recorrido(ruta, calculador.costoRuta(origen, destino));
    }

    public boolean estaAbierto(Pasillo pasillo) {
        Objects.requireNonNull(pasillo, "El pasillo es obligatorio.");

        return calculador.estaAbierto(
                pasillo.origen(), pasillo.destino()
        );
    }

    public void cambiarEstadoPasillo(Pasillo pasillo, boolean abierto) {
        Objects.requireNonNull(pasillo, "El pasillo es obligatorio.");

        calculador.cambiarEstadoPasillo(
                pasillo.origen(), pasillo.destino(), abierto
        );
    }

    public Pasillo[] consultarPasillosCerrados() {
        Pasillo[] temporal = new Pasillo[pasillos.length];
        int cantidad = 0;

        for (Pasillo pasillo : pasillos) {
            if (!estaAbierto(pasillo)) {
                temporal[cantidad++] = pasillo;
            }
        }

        Pasillo[] cerrados = new Pasillo[cantidad];
        System.arraycopy(temporal, 0, cerrados, 0, cantidad);

        return cerrados;
    }

    public String[] consultarApartamentosInalcanzables() {
        String[] inalcanzables =
                calculador.consultarInalcanzables(consultarRecepcion());

        String[] temporal = new String[inalcanzables.length];
        int cantidad = 0;

        for (String codigo : inalcanzables) {
            for (Ubicacion ubicacion : ubicaciones) {
                if (codigo.equals(ubicacion.id())
                        && "APARTAMENTO".equals(ubicacion.tipo())) {
                    temporal[cantidad++] = codigo;
                    break;
                }
            }
        }

        String[] apartamentos = new String[cantidad];
        System.arraycopy(temporal, 0, apartamentos, 0, cantidad);

        return apartamentos;
    }
}
