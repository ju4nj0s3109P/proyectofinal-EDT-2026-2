package co.edu.uq.sga.estructuras.grafos;

import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.util.Iterator;

public final class Ruta<T> implements Iterable<T> {

    private final ListaEnlazada<T> camino;
    private final double costo;

    Ruta(ListaEnlazada<T> camino, double costo) {
        this.camino = camino;
        this.costo = costo;
    }

    static <T> Ruta<T> vacia() {
        return new Ruta<>(new ListaEnlazada<>(), Double.POSITIVE_INFINITY);
    }

    public boolean existe() {
        return !camino.estaVacia();
    }

    public ListaEnlazada<T> getCamino() {
        return camino.copiar();
    }

    public double getCosto() {
        return costo;
    }

    public int cantidadVertices() {
        return camino.tamanio();
    }

    @Override
    public Iterator<T> iterator() {
        return camino.copiar().iterator();
    }

    @Override
    public String toString() {
        return existe() ? camino + " costo=" + costo : "sin ruta";
    }
}
