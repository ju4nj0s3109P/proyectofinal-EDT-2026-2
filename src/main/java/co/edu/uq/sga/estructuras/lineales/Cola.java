package co.edu.uq.sga.estructuras.lineales;

import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

public class Cola<T> implements Iterable<T> {

    private static final class Nodo<T> {
        private final T dato;
        private Nodo<T> siguiente;

        private Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamanio;

    public void encolar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("La cola no admite elementos nulos.");
        }
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (fin == null) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamanio++;
    }

    public T desencolar() {
        exigirNoVacia();
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamanio--;
        return dato;
    }

    public T verFrente() {
        exigirNoVacia();
        return frente.dato;
    }

    public boolean contiene(Predicate<? super T> condicion) {
        Objects.requireNonNull(condicion, "La condición es obligatoria.");
        for (Nodo<T> n = frente; n != null; n = n.siguiente) {
            if (condicion.test(n.dato)) {
                return true;
            }
        }
        return false;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public void limpiar() {
        frente = null;
        fin = null;
        tamanio = 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> actual = frente;

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
                actual = actual.siguiente;
                return dato;
            }
        };
    }

    private void exigirNoVacia() {
        if (tamanio == 0) {
            throw new EstructuraVaciaException("La cola está vacía.");
        }
    }
}
