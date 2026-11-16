package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.contratos.ColaReservasPendientes;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.estructuras.lineales.ColaPrioridad;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.util.Comparator;
import java.util.Iterator;

/** Cola de prioridad propia con las reservas pendientes: atiende primero la que vence antes. */
public class ColaReservasPendientesPorVencimiento
        implements ColaReservasPendientes, Iterable<Reserva> {

    private final ColaPrioridad<Reserva> cola;

    /** Recibe el criterio de vencimiento para no repetir la regla de las 24 horas. */
    public ColaReservasPendientesPorVencimiento(Comparator<Reserva> porVencimiento) {
        if (porVencimiento == null) {
            throw new IllegalArgumentException("El criterio de vencimiento es obligatorio.");
        }
        this.cola = new ColaPrioridad<>(porVencimiento);
    }

    @Override
    public void encolar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La reserva es obligatoria.");
        }
        cola.insertar(reserva);
    }

    @Override
    public boolean estaVacia() {
        return cola.estaVacia();
    }

    @Override
    public Reserva verProxima() {
        return cola.verPrimero();
    }

    @Override
    public Reserva desencolar() {
        return cola.extraerPrimero();
    }

    @Override
    public boolean retirar(Reserva reserva) {
        return reserva != null && cola.eliminar(reserva);
    }

    public int tamanio() {
        return cola.tamanio();
    }

    /** Las pendientes en orden de vencimiento, para mostrarlas en la interfaz. */
    public ListaEnlazada<Reserva> enOrdenDeVencimiento() {
        return cola.enOrden();
    }

    @Override
    public Iterator<Reserva> iterator() {
        return cola.iterator();
    }
}
