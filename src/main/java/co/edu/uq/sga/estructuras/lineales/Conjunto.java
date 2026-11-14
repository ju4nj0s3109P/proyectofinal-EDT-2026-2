package co.edu.uq.sga.estructuras.lineales;

import java.util.Iterator;

public class Conjunto<T> implements Iterable<T> {

    private final ListaEnlazada<T> elementos = new ListaEnlazada<>();

    public boolean agregar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("El conjunto no admite elementos nulos.");
        }
        if (elementos.contiene(elemento)) {
            return false;
        }
        elementos.agregarAlFinal(elemento);
        return true;
    }

    public boolean eliminar(T elemento) {
        return elementos.eliminar(elemento);
    }

    public boolean contiene(T elemento) {
        return elementos.contiene(elemento);
    }

    public int tamanio() {
        return elementos.tamanio();
    }

    public boolean estaVacio() {
        return elementos.estaVacia();
    }

    public void limpiar() {
        elementos.limpiar();
    }

    public Conjunto<T> union(Conjunto<T> otro) {
        Conjunto<T> resultado = new Conjunto<>();
        for (T e : this) {
            resultado.agregar(e);
        }
        for (T e : otro) {
            resultado.agregar(e);
        }
        return resultado;
    }

    public Conjunto<T> interseccion(Conjunto<T> otro) {
        Conjunto<T> resultado = new Conjunto<>();
        for (T e : this) {
            if (otro.contiene(e)) {
                resultado.agregar(e);
            }
        }
        return resultado;
    }

    public ListaEnlazada<T> aLista() {
        return elementos.copiar();
    }

    @Override
    public Iterator<T> iterator() {
        return elementos.iterator();
    }

    @Override
    public String toString() {
        return "{" + elementos.toString().substring(1, elementos.toString().length() - 1) + "}";
    }
}
