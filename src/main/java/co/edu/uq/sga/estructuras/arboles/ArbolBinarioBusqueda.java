package co.edu.uq.sga.estructuras.arboles;

import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import co.edu.uq.sga.estructuras.lineales.Pila;
import java.util.Iterator;
import java.util.Optional;

public class ArbolBinarioBusqueda<T extends Comparable<T>> implements Iterable<T> {

    private static final class Nodo<T> {
        private T dato;
        private Nodo<T> izquierdo;
        private Nodo<T> derecho;

        private Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> raiz;
    private int tamanio;

    private boolean modificado;

    // ------------------------------------------------------------ inserción

    public boolean insertar(T elemento) {
        validar(elemento);
        modificado = false;
        raiz = insertar(raiz, elemento);
        if (modificado) {
            tamanio++;
        }
        return modificado;
    }

    private Nodo<T> insertar(Nodo<T> nodo, T elemento) {
        if (nodo == null) {
            modificado = true;
            return new Nodo<>(elemento);
        }
        int comparacion = elemento.compareTo(nodo.dato);
        if (comparacion < 0) {
            nodo.izquierdo = insertar(nodo.izquierdo, elemento);
        } else if (comparacion > 0) {
            nodo.derecho = insertar(nodo.derecho, elemento);
        }
        // comparacion == 0: duplicado, no se inserta
        return nodo;
    }

    // ------------------------------------------------------------- búsqueda

    public Optional<T> buscar(T sonda) {
        validar(sonda);
        return Optional.ofNullable(buscar(raiz, sonda));
    }

    private T buscar(Nodo<T> nodo, T sonda) {
        if (nodo == null) {
            return null;
        }
        int comparacion = sonda.compareTo(nodo.dato);
        if (comparacion == 0) {
            return nodo.dato;
        }
        return comparacion < 0 ? buscar(nodo.izquierdo, sonda) : buscar(nodo.derecho, sonda);
    }

    public boolean contiene(T sonda) {
        return buscar(sonda).isPresent();
    }

    public T minimo() {
        exigirNoVacio();
        return minimo(raiz).dato;
    }

    private Nodo<T> minimo(Nodo<T> nodo) {
        return nodo.izquierdo == null ? nodo : minimo(nodo.izquierdo);
    }

    public T maximo() {
        exigirNoVacio();
        return maximo(raiz).dato;
    }

    private Nodo<T> maximo(Nodo<T> nodo) {
        return nodo.derecho == null ? nodo : maximo(nodo.derecho);
    }

    // ------------------------------------------------------------ eliminación

    public boolean eliminar(T sonda) {
        validar(sonda);
        modificado = false;
        raiz = eliminar(raiz, sonda);
        if (modificado) {
            tamanio--;
        }
        return modificado;
    }

    private Nodo<T> eliminar(Nodo<T> nodo, T sonda) {
        if (nodo == null) {
            return null;
        }
        int comparacion = sonda.compareTo(nodo.dato);
        if (comparacion < 0) {
            nodo.izquierdo = eliminar(nodo.izquierdo, sonda);
            return nodo;
        }
        if (comparacion > 0) {
            nodo.derecho = eliminar(nodo.derecho, sonda);
            return nodo;
        }
        modificado = true;
        if (nodo.izquierdo == null) {
            return nodo.derecho;
        }
        if (nodo.derecho == null) {
            return nodo.izquierdo;
        }
        Nodo<T> sucesor = minimo(nodo.derecho);
        nodo.dato = sucesor.dato;
        nodo.derecho = eliminar(nodo.derecho, sucesor.dato);
        return nodo;
    }

    // -------------------------------------------------------------- medidas

    public int altura() {
        return altura(raiz);
    }

    private int altura(Nodo<T> nodo) {
        if (nodo == null) {
            return 0;
        }
        return 1 + Math.max(altura(nodo.izquierdo), altura(nodo.derecho));
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacio() {
        return tamanio == 0;
    }

    public void limpiar() {
        raiz = null;
        tamanio = 0;
    }

    // ------------------------------------------------- consultas ordenadas

    public ListaEnlazada<T> rango(T desde, T hasta) {
        validar(desde);
        validar(hasta);
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        if (desde.compareTo(hasta) < 0) {
            rango(raiz, desde, hasta, resultado);
        }
        return resultado;
    }

    private void rango(Nodo<T> nodo, T desde, T hasta, ListaEnlazada<T> resultado) {
        if (nodo == null) {
            return;
        }
        boolean mayorOIgualQueDesde = nodo.dato.compareTo(desde) >= 0;
        boolean menorQueHasta = nodo.dato.compareTo(hasta) < 0;
        if (mayorOIgualQueDesde) {
            rango(nodo.izquierdo, desde, hasta, resultado); // a la izquierda puede haber más
        }
        if (mayorOIgualQueDesde && menorQueHasta) {
            resultado.agregarAlFinal(nodo.dato);
        }
        if (menorQueHasta) {
            rango(nodo.derecho, desde, hasta, resultado); // a la derecha puede haber más
        }
    }

    public Optional<T> anterior(T referencia) {
        validar(referencia);
        return Optional.ofNullable(anterior(raiz, referencia, null));
    }

    private T anterior(Nodo<T> nodo, T referencia, T mejor) {
        if (nodo == null) {
            return mejor;
        }
        if (nodo.dato.compareTo(referencia) < 0) {
            // candidato válido; puede haber uno más cercano a la derecha
            return anterior(nodo.derecho, referencia, nodo.dato);
        }
        return anterior(nodo.izquierdo, referencia, mejor);
    }

    public Optional<T> siguiente(T referencia) {
        validar(referencia);
        return Optional.ofNullable(siguiente(raiz, referencia, null));
    }

    private T siguiente(Nodo<T> nodo, T referencia, T mejor) {
        if (nodo == null) {
            return mejor;
        }
        if (nodo.dato.compareTo(referencia) > 0) {
            return siguiente(nodo.izquierdo, referencia, nodo.dato);
        }
        return siguiente(nodo.derecho, referencia, mejor);
    }

    // ------------------------------------------------------------- recorridos

    public ListaEnlazada<T> inorden() {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        inorden(raiz, resultado);
        return resultado;
    }

    private void inorden(Nodo<T> nodo, ListaEnlazada<T> resultado) {
        if (nodo == null) {
            return;
        }
        inorden(nodo.izquierdo, resultado);
        resultado.agregarAlFinal(nodo.dato);
        inorden(nodo.derecho, resultado);
    }

    public ListaEnlazada<T> preorden() {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        preorden(raiz, resultado);
        return resultado;
    }

    private void preorden(Nodo<T> nodo, ListaEnlazada<T> resultado) {
        if (nodo == null) {
            return;
        }
        resultado.agregarAlFinal(nodo.dato);
        preorden(nodo.izquierdo, resultado);
        preorden(nodo.derecho, resultado);
    }

    public ListaEnlazada<T> postorden() {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        postorden(raiz, resultado);
        return resultado;
    }

    private void postorden(Nodo<T> nodo, ListaEnlazada<T> resultado) {
        if (nodo == null) {
            return;
        }
        postorden(nodo.izquierdo, resultado);
        postorden(nodo.derecho, resultado);
        resultado.agregarAlFinal(nodo.dato);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private final Pila<Nodo<T>> pendientes = new Pila<>();

            {
                apilarIzquierdos(raiz);
            }

            private void apilarIzquierdos(Nodo<T> nodo) {
                for (Nodo<T> n = nodo; n != null; n = n.izquierdo) {
                    pendientes.apilar(n);
                }
            }

            @Override
            public boolean hasNext() {
                return !pendientes.estaVacia();
            }

            @Override
            public T next() {
                if (pendientes.estaVacia()) {
                    throw new EstructuraVaciaException("No quedan elementos por recorrer.");
                }
                Nodo<T> actual = pendientes.desapilar();
                apilarIzquierdos(actual.derecho);
                return actual.dato;
            }
        };
    }

    // ------------------------------------------------------------ auxiliares

    private void exigirNoVacio() {
        if (tamanio == 0) {
            throw new EstructuraVaciaException("El árbol está vacío.");
        }
    }

    private static void validar(Object elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("El árbol no admite elementos nulos.");
        }
    }
}
