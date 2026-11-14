package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.contratos.IndiceReservas;
import co.edu.uq.sga.dominio.modelo.AsignacionApartamento;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.estructuras.arboles.ArbolBinarioBusqueda;
import co.edu.uq.sga.estructuras.arboles.DiccionarioArbol;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.time.LocalDate;

public class IndiceReservasArbol implements IndiceReservas {

    private static final class EntradaReserva implements Comparable<EntradaReserva> {
        private final LocalDate entrada;
        private final LocalDate salida;
        private final String folio;
        private final Reserva reserva;

        private EntradaReserva(Reserva reserva) {
            this(reserva.getEstancia().getFechaEntrada(), reserva.getEstancia().getFechaSalida(),
                    reserva.getFolio(), reserva);
        }

        private EntradaReserva(LocalDate entrada, LocalDate salida, String folio, Reserva reserva) {
            this.entrada = entrada;
            this.salida = salida;
            this.folio = folio;
            this.reserva = reserva;
        }

        private static EntradaReserva sonda(LocalDate fecha, String folio) {
            return new EntradaReserva(fecha, fecha, folio, null);
        }

        @Override
        public int compareTo(EntradaReserva otra) {
            int porEntrada = entrada.compareTo(otra.entrada);
            return porEntrada != 0 ? porEntrada : folio.compareTo(otra.folio);
        }
    }

    private final DiccionarioArbol<String, ArbolBinarioBusqueda<EntradaReserva>> porApartamento =
            new DiccionarioArbol<>();

    @Override
    public boolean agregarSiDisponible(Reserva reserva) {
        validar(reserva);
        if (algunaIndexada(reserva) || !estaDisponible(reserva)) {
            return false;
        }
        for (AsignacionApartamento asignacion : reserva.getAsignaciones()) {
            arbolDe(codigo(asignacion), true).insertar(new EntradaReserva(reserva));
        }
        return true;
    }

    @Override
    public boolean retirarSiExiste(Reserva reserva) {
        validar(reserva);
        if (!contiene(reserva)) {
            return false;
        }
        LocalDate entrada = reserva.getEstancia().getFechaEntrada();
        for (AsignacionApartamento asignacion : reserva.getAsignaciones()) {
            String codigo = codigo(asignacion);
            ArbolBinarioBusqueda<EntradaReserva> arbol = arbolDe(codigo, false);
            arbol.eliminar(EntradaReserva.sonda(entrada, reserva.getFolio()));
            if (arbol.estaVacio()) {
                porApartamento.quitar(codigo);
            }
        }
        return true;
    }

    @Override
    public boolean estaDisponible(Reserva reserva) {
        validar(reserva);
        LocalDate entrada = reserva.getEstancia().getFechaEntrada();
        LocalDate salida = reserva.getEstancia().getFechaSalida();
        for (AsignacionApartamento asignacion : reserva.getAsignaciones()) {
            ArbolBinarioBusqueda<EntradaReserva> arbol = arbolDe(codigo(asignacion), false);
            if (arbol != null && hayChoque(arbol, entrada, salida, reserva.getFolio())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean contiene(Reserva reserva) {
        validar(reserva);
        for (AsignacionApartamento asignacion : reserva.getAsignaciones()) {
            if (!indexadaEn(reserva, codigo(asignacion))) {
                return false;
            }
        }
        return true;
    }

    public ListaEnlazada<Reserva> reservasDe(String codigoApartamento) {
        ListaEnlazada<Reserva> resultado = new ListaEnlazada<>();
        ArbolBinarioBusqueda<EntradaReserva> arbol = arbolDe(codigoApartamento, false);
        if (arbol != null) {
            for (EntradaReserva entrada : arbol) {
                resultado.agregarAlFinal(entrada.reserva);
            }
        }
        return resultado;
    }

    public ListaEnlazada<String> apartamentosConReservas() {
        return porApartamento.claves();
    }

    /**
     * El rango recursivo del árbol obtiene las entradas ordenadas por fecha.
     * Se comprueba el intervalo semiabierto completo para no depender de una
     * única reserva anterior, que podría no ser la de mayor duración.
     */
    private static boolean hayChoque(ArbolBinarioBusqueda<EntradaReserva> arbol,
            LocalDate entrada, LocalDate salida, String folio) {
        EntradaReserva limiteInferior = EntradaReserva.sonda(LocalDate.MIN, "");
        EntradaReserva limiteSuperior = EntradaReserva.sonda(LocalDate.MAX, "\uFFFF");
        for (EntradaReserva existente : arbol.rango(limiteInferior, limiteSuperior)) {
            if (!existente.folio.equals(folio)
                    && entrada.isBefore(existente.salida)
                    && existente.entrada.isBefore(salida)) {
                return true;
            }
        }
        return false;
    }

    private boolean algunaIndexada(Reserva reserva) {
        for (AsignacionApartamento asignacion : reserva.getAsignaciones()) {
            if (indexadaEn(reserva, codigo(asignacion))) {
                return true;
            }
        }
        return false;
    }

    private boolean indexadaEn(Reserva reserva, String codigo) {
        ArbolBinarioBusqueda<EntradaReserva> arbol = arbolDe(codigo, false);
        if (arbol == null) {
            return false;
        }
        return arbol.buscar(EntradaReserva.sonda(
                        reserva.getEstancia().getFechaEntrada(), reserva.getFolio()))
                .isPresent();
    }

    private ArbolBinarioBusqueda<EntradaReserva> arbolDe(String codigo, boolean crear) {
        if (crear) {
            return porApartamento.obtenerOCrear(codigo, ArbolBinarioBusqueda::new);
        }
        return porApartamento.obtener(codigo).orElse(null);
    }

    private static String codigo(AsignacionApartamento asignacion) {
        return asignacion.getApartamento().getCodigo();
    }

    private static void validar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria.");
        }
    }
}
