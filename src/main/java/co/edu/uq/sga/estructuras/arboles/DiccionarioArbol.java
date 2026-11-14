package co.edu.uq.sga.estructuras.arboles;

import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class DiccionarioArbol<K extends Comparable<K>, V>
        implements Iterable<DiccionarioArbol.Entrada<K, V>> {

    public static final class Entrada<K extends Comparable<K>, V>
            implements Comparable<Entrada<K, V>> {
        private final K clave;
        private V valor;

        private Entrada(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }

        public K getClave() {
            return clave;
        }

        public V getValor() {
            return valor;
        }

        @Override
        public int compareTo(Entrada<K, V> otra) {
            return clave.compareTo(otra.clave);
        }

        @Override
        public String toString() {
            return clave + "=" + valor;
        }
    }

    private final ArbolBinarioBusqueda<Entrada<K, V>> arbol = new ArbolBinarioBusqueda<>();

    public Optional<V> poner(K clave, V valor) {
        Optional<Entrada<K, V>> existente = arbol.buscar(sonda(clave));
        if (existente.isPresent()) {
            V anterior = existente.get().valor;
            existente.get().valor = valor;
            return Optional.ofNullable(anterior);
        }
        arbol.insertar(new Entrada<>(clave, valor));
        return Optional.empty();
    }

    public Optional<V> obtener(K clave) {
        return arbol.buscar(sonda(clave)).map(Entrada::getValor);
    }

    public V obtenerOCrear(K clave, Supplier<? extends V> fabrica) {
        Objects.requireNonNull(fabrica, "La fábrica es obligatoria.");
        Optional<Entrada<K, V>> existente = arbol.buscar(sonda(clave));
        if (existente.isPresent()) {
            return existente.get().valor;
        }
        V nuevo = fabrica.get();
        arbol.insertar(new Entrada<>(clave, nuevo));
        return nuevo;
    }

    public boolean contieneClave(K clave) {
        return arbol.contiene(sonda(clave));
    }

    public Optional<V> quitar(K clave) {
        Optional<V> valor = obtener(clave);
        arbol.eliminar(sonda(clave));
        return valor;
    }

    public ListaEnlazada<K> claves() {
        ListaEnlazada<K> claves = new ListaEnlazada<>();
        for (Entrada<K, V> e : arbol) {
            claves.agregarAlFinal(e.clave);
        }
        return claves;
    }

    public ListaEnlazada<V> valores() {
        ListaEnlazada<V> valores = new ListaEnlazada<>();
        for (Entrada<K, V> e : arbol) {
            if (e.valor != null) {
                valores.agregarAlFinal(e.valor);
            }
        }
        return valores;
    }

    public int tamanio() {
        return arbol.tamanio();
    }

    public boolean estaVacio() {
        return arbol.estaVacio();
    }

    public void limpiar() {
        arbol.limpiar();
    }

    @Override
    public Iterator<Entrada<K, V>> iterator() {
        return arbol.iterator();
    }

    private Entrada<K, V> sonda(K clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave es obligatoria.");
        }
        return new Entrada<>(clave, null);
    }
}
