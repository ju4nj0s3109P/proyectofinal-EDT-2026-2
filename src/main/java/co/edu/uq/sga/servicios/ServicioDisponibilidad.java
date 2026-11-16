package co.edu.uq.sga.servicios;

import co.edu.uq.sga.dominio.contratos.ReservasPorApartamento;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.util.Comparator;

/**
 * Busca los apartamentos libres de una estancia y arma la asignación de un grupo
 * grande. Solo consulta las reservas del apartamento que pregunta, no todas las
 * del alojamiento.
 */
public class ServicioDisponibilidad {

    private final ReservasPorApartamento reservas;

    public ServicioDisponibilidad(ReservasPorApartamento reservas) {
        if (reservas == null) {
            throw new IllegalArgumentException("La consulta de reservas es obligatoria.");
        }
        this.reservas = reservas;
    }

    /** Un apartamento está libre si está operativo y ninguna reserva activa comparte noche. */
    public boolean estaLibre(Apartamento apartamento, Estancia estancia) {
        if (apartamento == null) {
            throw new IllegalArgumentException("El apartamento es obligatorio.");
        }
        if (estancia == null) {
            throw new IllegalArgumentException("La estancia es obligatoria.");
        }
        if (apartamento.getEstadoOperativo() == EstadoApartamento.FUERA_DE_SERVICIO) {
            return false;
        }
        for (Reserva reserva : reservas.de(apartamento.getCodigo())) {
            if (estancia.seSolapaCon(reserva.getEstancia())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Apartamentos libres en los que cabe el grupo completo, en el orden pedido
     * (precio, capacidad o dormitorios con {@link ComparadoresApartamento}).
     *
     * @param orden criterio de orden; con null se usa el orden natural por código
     */
    public ListaEnlazada<Apartamento> buscarDisponibles(
            Apartamento[] catalogo, Estancia estancia, int personas,
            Comparator<Apartamento> orden) {
        if (personas <= 0) {
            throw new IllegalArgumentException("El número de personas debe ser mayor que cero.");
        }
        ListaEnlazada<Apartamento> disponibles =
                libres(catalogo, estancia).filtrar(a -> a.getCapacidadMaxima() >= personas);
        disponibles.ordenar(orden == null ? Comparator.naturalOrder() : orden);
        return disponibles;
    }

    /**
     * Combinación de apartamentos libres que aloja al grupo con el menor número de
     * apartamentos, buscada con recursividad y backtracking. Se usa cuando el grupo
     * no cabe en un solo apartamento.
     *
     * @return la combinación encontrada, o una lista vacía si el grupo no cabe
     */
    public ListaEnlazada<Apartamento> asignarGrupo(
            Apartamento[] catalogo, Estancia estancia, int personas) {
        if (personas <= 0) {
            throw new IllegalArgumentException("El número de personas debe ser mayor que cero.");
        }
        // De mayor capacidad a menor: así la primera combinación completa ya es corta y poda el resto.
        ListaEnlazada<Apartamento> libres = libres(catalogo, estancia);
        libres.ordenar(ComparadoresApartamento.porCapacidad().reversed());
        Apartamento[] candidatos = libres.aArreglo(Apartamento[]::new);

        ListaEnlazada<Apartamento> mejor = new ListaEnlazada<>();
        buscarCombinacion(candidatos, 0, personas, new Apartamento[candidatos.length], 0,
                new int[] {Integer.MAX_VALUE}, mejor);
        return mejor;
    }

    private static void buscarCombinacion(Apartamento[] candidatos, int desde, int faltan,
            Apartamento[] actual, int usados, int[] mejorCantidad,
            ListaEnlazada<Apartamento> mejor) {
        if (faltan <= 0) {
            mejorCantidad[0] = usados;
            mejor.limpiar();
            for (int i = 0; i < usados; i++) {
                mejor.agregarAlFinal(actual[i]);
            }
            return;
        }
        // Añadir uno más ya empataría o superaría la mejor combinación conocida.
        if (usados + 1 >= mejorCantidad[0]) {
            return;
        }
        for (int i = desde; i < candidatos.length; i++) {
            actual[usados] = candidatos[i];
            buscarCombinacion(candidatos, i + 1, faltan - candidatos[i].getCapacidadMaxima(),
                    actual, usados + 1, mejorCantidad, mejor);
        }
    }

    private ListaEnlazada<Apartamento> libres(Apartamento[] catalogo, Estancia estancia) {
        if (catalogo == null) {
            throw new IllegalArgumentException("El catálogo de apartamentos es obligatorio.");
        }
        ListaEnlazada<Apartamento> libres = new ListaEnlazada<>();
        for (Apartamento apartamento : catalogo) {
            if (apartamento == null) {
                throw new IllegalArgumentException("Los apartamentos del catálogo no pueden ser nulos.");
            }
            if (estaLibre(apartamento, estancia)) {
                libres.agregarAlFinal(apartamento);
            }
        }
        return libres;
    }
}
