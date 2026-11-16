package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.contratos.RegistroConflictosReservas;
import co.edu.uq.sga.dominio.modelo.ConflictoReserva;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.util.Iterator;

/**
 * Guarda los conflictos de canal en la lista doblemente enlazada propia, en orden
 * de aparición, para el reporte de conflictos del administrador.
 */
public class RegistroConflictosEnLista
        implements RegistroConflictosReservas, Iterable<ConflictoReserva> {

    private final ListaEnlazada<ConflictoReserva> conflictos = new ListaEnlazada<>();

    @Override
    public void registrar(ConflictoReserva conflicto) {
        if (conflicto == null) {
            throw new IllegalArgumentException("El conflicto es obligatorio.");
        }
        conflictos.agregarAlFinal(conflicto);
    }

    /** Copia de los conflictos registrados; el registro no se modifica desde fuera. */
    public ListaEnlazada<ConflictoReserva> conflictos() {
        return conflictos.copiar();
    }

    /** Conflictos de un apartamento, para el reporte por apartamento. */
    public ListaEnlazada<ConflictoReserva> conflictosDe(String codigoApartamento) {
        return conflictos.filtrar(conflicto -> contiene(conflicto, codigoApartamento));
    }

    public int tamanio() {
        return conflictos.tamanio();
    }

    @Override
    public Iterator<ConflictoReserva> iterator() {
        return conflictos.iterator();
    }

    private static boolean contiene(ConflictoReserva conflicto, String codigoApartamento) {
        for (String codigo : conflicto.getCodigosApartamentos()) {
            if (codigo.equals(codigoApartamento)) {
                return true;
            }
        }
        return false;
    }
}
