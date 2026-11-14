package co.edu.uq.sga.estructuras.lineales;

import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import java.util.Iterator;

public class ListaCircular<T> implements Iterable<T> {

    private static final class Nodo<T> {
        private final T dato;
        private Nodo<T> anterior;
        private Nodo<T> siguiente;

        private Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> primero;
    private Nodo<T> actual;
    private int tamanio;

    public void agregar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("La lista circular no admite elementos nulos.");
        }
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (primero == null) {
            nuevo.anterior = nuevo;
            nuevo.siguiente = nuevo;
            primero = nuevo;
            actual = nuevo;
        } else {
            Nodo<T> ultimo = primero.anterior;
            nuevo.anterior = ultimo;
            nuevo.siguiente = primero;
            ultimo.siguiente = nuevo;
            primero.anterior = nuevo;
        }
        tamanio++;
    }

    public T actual() {
        exigirNoVacia();
        return actual.dato;
    }

    public T avanzar() {
        exigirNoVacia();
        actual = actual.siguiente;
        return actual.dato;
    }

    public T retroceder() {
        exigirNoVacia();
        actual = actual.anterior;
        return actual.dato;
    }

    public T verSiguiente() {
        exigirNoVacia();
        return actual.siguiente.dato;
    }

    public boolean eliminar(T elemento) {
        Nodo<T> nodo = buscarNodo(elemento);
        if (nodo == null) {
            return false;
        }
        if (tamanio == 1) {
            actual = null;
            primero = null;
        } else {
            nodo.anterior.siguiente = nodo.siguiente;
            nodo.siguiente.anterior = nodo.anterior;
            if (nodo == actual) {
                actual = nodo.siguiente;
            }
            if (nodo == primero) {
                primero = nodo.siguiente;
            }
        }
        tamanio--;
        return true;
    }

    public boolean contiene(T elemento) {
        return buscarNodo(elemento) != null;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Nodo<T> siguiente = actual;
            private int restantes = tamanio;

            @Override
            public boolean hasNext() {
                return restantes > 0;
            }

            @Override
            public T next() {
                if (restantes == 0) {
                    throw new EstructuraVaciaException("Ya se recorrió la vuelta completa.");
                }
                T dato = siguiente.dato;
                siguiente = siguiente.siguiente;
                restantes--;
                return dato;
            }
        };
    }

    private Nodo<T> buscarNodo(T elemento) {
        if (elemento == null || actual == null) {
            return null;
        }
        Nodo<T> n = actual;
        for (int i = 0; i < tamanio; i++) {
            if (n.dato.equals(elemento)) {
                return n;
            }
            n = n.siguiente;
        }
        return null;
    }

    private void exigirNoVacia() {
        if (tamanio == 0) {
            throw new EstructuraVaciaException("La lista circular está vacía.");
        }
    }
}
