package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.contratos.ColaSolicitudesExternas;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.estructuras.lineales.Cola;
import java.util.Iterator;

public class ColaSolicitudesExternasEnCola implements ColaSolicitudesExternas, Iterable<Reserva> {

    private final Cola<Reserva> cola = new Cola<>();

    @Override
    public void encolar(Reserva reserva) {
        if (reserva == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria.");
        }
        cola.encolar(reserva);
    }

    @Override
    public boolean estaVacia() {
        return cola.estaVacia();
    }

    @Override
    public boolean contieneFolio(String folio) {
        if (folio == null) {
            return false;
        }
        String buscado = folio.trim();
        return cola.contiene(r -> r.getFolio().equals(buscado));
    }

    @Override
    public Reserva desencolar() {
        return cola.desencolar();
    }

    public int tamanio() {
        return cola.tamanio();
    }

    @Override
    public Iterator<Reserva> iterator() {
        return cola.iterator();
    }
}
