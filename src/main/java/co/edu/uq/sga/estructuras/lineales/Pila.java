package co.edu.uq.sga.estructuras.lineales;

import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import java.util.Iterator;

public class Pila<T> implements Iterable<T> {

    private static final class Nodo<T> {
        private final T dato;
        private final Nodo<T> debajo;

        private Nodo(T dato, Nodo<T> debajo) {
            this.dato = dato;
            this.debajo = debajo;
        }
    }

    private Nodo<T> tope;
    private int tamanio;

    public void apilar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("La pila no admite elementos nulos.");
        }
        tope = new Nodo<>(elemento, tope);
        tamanio++;
    }

    public T desapilar() {
        exigirNoVacia();
        T dato = tope.dato;
        tope = tope.debajo;
        tamanio--;
        return dato;
    }

    public T verTope() {
        exigirNoVacia();
        return tope.dato;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public void limpiar() {
        tope = null;
        tamanio = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> actual = tope;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new EstructuraVaciaException("No quedan elementos por recorrer.");
                }
                T dato = actual.dato;
                actual = actual.debajo;
                return dato;
            }
        };
    }

    private void exigirNoVacia() {
        if (tamanio == 0) {
            throw new EstructuraVaciaException("La pila está vacía.");
        }
    }
}
