package co.edu.uq.sga.servicios;

import co.edu.uq.sga.adaptadores.CalculadorRutasGrafo;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.util.Objects;

/** Consultas del mapa y análisis administrativo de conectividad del edificio. */
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
    public record GrupoBloque(String bloque, String[] codigosApartamentos) {
        public GrupoBloque { codigosApartamentos = codigosApartamentos.clone(); }
        @Override public String[] codigosApartamentos() { return codigosApartamentos.clone(); }
    }

    private static final class AcumuladorBloque {
        private final String bloque;
        private final ListaEnlazada<String> codigos = new ListaEnlazada<>();
        private AcumuladorBloque(String bloque) { this.bloque = bloque; }
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
            if ("RECEPCION".equals(ubicacion.tipo())) return ubicacion.id();
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
        return calculador.estaAbierto(pasillo.origen(), pasillo.destino());
    }

    public void cambiarEstadoPasillo(Pasillo pasillo, boolean abierto) {
        Objects.requireNonNull(pasillo, "El pasillo es obligatorio.");
        calculador.cambiarEstadoPasillo(pasillo.origen(), pasillo.destino(), abierto);
    }

    public Pasillo[] consultarPasillosCerrados() {
        Pasillo[] temporal = new Pasillo[pasillos.length];
        int cantidad = 0;
        for (Pasillo pasillo : pasillos) {
            if (!estaAbierto(pasillo)) temporal[cantidad++] = pasillo;
        }
        Pasillo[] cerrados = new Pasillo[cantidad];
        System.arraycopy(temporal, 0, cerrados, 0, cantidad);
        return cerrados;
    }

    public String[] consultarApartamentosInalcanzables() {
        String[] inalcanzables = calculador.consultarInalcanzables(consultarRecepcion());
        String[] temporal = new String[inalcanzables.length];
        int cantidad = 0;
        for (String codigo : inalcanzables) {
            for (Ubicacion ubicacion : ubicaciones) {
                if (codigo.equals(ubicacion.id()) && "APARTAMENTO".equals(ubicacion.tipo())) {
                    temporal[cantidad++] = codigo;
                    break;
                }
            }
        }
        String[] apartamentos = new String[cantidad];
        System.arraycopy(temporal, 0, apartamentos, 0, cantidad);
        return apartamentos;
    }

    /** Devuelve el orden BFS o DFS de las ubicaciones alcanzables desde recepción. */
    public String[] consultarRecorridoAdministrativo(boolean profundidad) {
        var grafo = calculador.getMapa();
        var recorrido = profundidad
                ? grafo.recorridoProfundidad(consultarRecepcion())
                : grafo.recorridoAnchura(consultarRecepcion());
        return recorrido.aArreglo(String[]::new);
    }

    /**
     * Agrupa todos los apartamentos por bloque. El orden dentro de cada bloque
     * sigue BFS o DFS para los nodos alcanzables; los inalcanzables se agregan
     * al final de su bloque para que el reporte no los oculte.
     */
    public GrupoBloque[] agruparApartamentosPorBloque(
            Apartamento[] catalogo, boolean profundidad) {
        Objects.requireNonNull(catalogo, "El catálogo de apartamentos es obligatorio.");
        ListaEnlazada<AcumuladorBloque> grupos = new ListaEnlazada<>();
        String[] recorrido = consultarRecorridoAdministrativo(profundidad);
        for (String codigo : recorrido) {
            Apartamento apartamento = buscar(catalogo, codigo);
            if (apartamento != null) agregarAGrupo(grupos, apartamento);
        }
        for (Apartamento apartamento : catalogo) {
            Objects.requireNonNull(apartamento, "El catálogo no admite apartamentos nulos.");
            if (!estaEnGrupos(grupos, apartamento.getCodigo())) agregarAGrupo(grupos, apartamento);
        }
        GrupoBloque[] resultado = new GrupoBloque[grupos.tamanio()];
        int indice = 0;
        for (AcumuladorBloque grupo : grupos) {
            resultado[indice++] = new GrupoBloque(
                    grupo.bloque, grupo.codigos.aArreglo(String[]::new));
        }
        return resultado;
    }

    private static Apartamento buscar(Apartamento[] catalogo, String codigo) {
        for (Apartamento apartamento : catalogo) {
            if (apartamento != null && apartamento.getCodigo().equals(codigo)) return apartamento;
        }
        return null;
    }

    private static void agregarAGrupo(ListaEnlazada<AcumuladorBloque> grupos, Apartamento apartamento) {
        AcumuladorBloque grupo = grupos.buscar(g -> g.bloque.equals(apartamento.getBloque())).orElse(null);
        if (grupo == null) {
            grupo = new AcumuladorBloque(apartamento.getBloque());
            grupos.agregarAlFinal(grupo);
        }
        if (!grupo.codigos.contiene(apartamento.getCodigo())) grupo.codigos.agregarAlFinal(apartamento.getCodigo());
    }

    private static boolean estaEnGrupos(ListaEnlazada<AcumuladorBloque> grupos, String codigo) {
        for (AcumuladorBloque grupo : grupos) {
            if (grupo.codigos.contiene(codigo)) return true;
        }
        return false;
    }
}
