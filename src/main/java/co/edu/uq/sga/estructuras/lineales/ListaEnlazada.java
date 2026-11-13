package co.edu.uq.sga.estructuras.lineales;

import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public class ListaEnlazada<T> implements Iterable<T> {

    private static final class Nodo<T> {
        private T dato;
        private Nodo<T> anterior;
        private Nodo<T> siguiente;

        private Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> cabeza;
    private Nodo<T> cola;
    private int tamanio;

    public ListaEnlazada() {
        // lista vacía
    }

    // ------------------------------------------------------------------ altas

    public void agregarAlInicio(T elemento) {
        Nodo<T> nuevo = new Nodo<>(validar(elemento));
        if (cabeza == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            nuevo.siguiente = cabeza;
            cabeza.anterior = nuevo;
            cabeza = nuevo;
        }
        tamanio++;
    }

    public void agregarAlFinal(T elemento) {
        Nodo<T> nuevo = new Nodo<>(validar(elemento));
        if (cola == null) {
            cabeza = nuevo;
            cola = nuevo;
        } else {
            nuevo.anterior = cola;
            cola.siguiente = nuevo;
            cola = nuevo;
        }
        tamanio++;
    }

    public void agregar(T elemento) {
        agregarAlFinal(elemento);
    }

    public void insertar(int indice, T elemento) {
        if (indice < 0 || indice > tamanio) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        if (indice == 0) {
            agregarAlInicio(elemento);
            return;
        }
        if (indice == tamanio) {
            agregarAlFinal(elemento);
            return;
        }
        Nodo<T> actual = nodoEn(indice);
        Nodo<T> nuevo = new Nodo<>(validar(elemento));
        nuevo.anterior = actual.anterior;
        nuevo.siguiente = actual;
        actual.anterior.siguiente = nuevo;
        actual.anterior = nuevo;
        tamanio++;
    }

    // -------------------------------------------------------------- consultas

    public T obtener(int indice) {
        verificarIndice(indice);
        return nodoEn(indice).dato;
    }

    public T reemplazar(int indice, T elemento) {
        verificarIndice(indice);
        Nodo<T> nodo = nodoEn(indice);
        T viejo = nodo.dato;
        nodo.dato = validar(elemento);
        return viejo;
    }

    public T primero() {
        exigirNoVacia();
        return cabeza.dato;
    }

    public T ultimo() {
        exigirNoVacia();
        return cola.dato;
    }

    public boolean contiene(T elemento) {
        return indiceDe(elemento) >= 0;
    }

    public int indiceDe(T elemento) {
        if (elemento == null) {
            return -1;
        }
        int indice = 0;
        for (Nodo<T> n = cabeza; n != null; n = n.siguiente) {
            if (n.dato.equals(elemento)) {
                return indice;
            }
            indice++;
        }
        return -1;
    }

    public Optional<T> buscar(Predicate<? super T> condicion) {
        Objects.requireNonNull(condicion, "La condición es obligatoria.");
        for (Nodo<T> n = cabeza; n != null; n = n.siguiente) {
            if (condicion.test(n.dato)) {
                return Optional.of(n.dato);
            }
        }
        return Optional.empty();
    }

    public boolean existe(Predicate<? super T> condicion) {
        return buscar(condicion).isPresent();
    }

    public ListaEnlazada<T> filtrar(Predicate<? super T> condicion) {
        Objects.requireNonNull(condicion, "La condición es obligatoria.");
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        for (Nodo<T> n = cabeza; n != null; n = n.siguiente) {
            if (condicion.test(n.dato)) {
                resultado.agregarAlFinal(n.dato);
            }
        }
        return resultado;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    // ------------------------------------------------------------------ bajas

    public T eliminarPrimero() {
        exigirNoVacia();
        return desenlazar(cabeza);
    }

    public T eliminarUltimo() {
        exigirNoVacia();
        return desenlazar(cola);
    }

    public T eliminarEn(int indice) {
        verificarIndice(indice);
        return desenlazar(nodoEn(indice));
    }

    public boolean eliminar(T elemento) {
        if (elemento == null) {
            return false;
        }
        for (Nodo<T> n = cabeza; n != null; n = n.siguiente) {
            if (n.dato.equals(elemento)) {
                desenlazar(n);
                return true;
            }
        }
        return false;
    }

    public int eliminarSi(Predicate<? super T> condicion) {
        Objects.requireNonNull(condicion, "La condición es obligatoria.");
        int eliminados = 0;
        Nodo<T> n = cabeza;
        while (n != null) {
            Nodo<T> siguiente = n.siguiente;
            if (condicion.test(n.dato)) {
                desenlazar(n);
                eliminados++;
            }
            n = siguiente;
        }
        return eliminados;
    }

    public void limpiar() {
        cabeza = null;
        cola = null;
        tamanio = 0;
    }

    // ------------------------------------------------------------- orden

    public void ordenar(Comparator<? super T> comparador) {
        Objects.requireNonNull(comparador, "El comparador es obligatorio.");
        if (tamanio < 2) {
            return;
        }
        cabeza = ordenarRecursivo(cabeza, comparador);
        // reconstruir enlaces hacia atrás y la cola
        Nodo<T> previo = null;
        for (Nodo<T> n = cabeza; n != null; n = n.siguiente) {
            n.anterior = previo;
            previo = n;
        }
        cola = previo;
    }

    public ListaEnlazada<T> ordenada(Comparator<? super T> comparador) {
        ListaEnlazada<T> copia = copiar();
        copia.ordenar(comparador);
        return copia;
    }

    public ListaEnlazada<T> copiar() {
        ListaEnlazada<T> copia = new ListaEnlazada<>();
        for (Nodo<T> n = cabeza; n != null; n = n.siguiente) {
            copia.agregarAlFinal(n.dato);
        }
        return copia;
    }

    private static <T> Nodo<T> ordenarRecursivo(Nodo<T> inicio, Comparator<? super T> c) {
        if (inicio == null || inicio.siguiente == null) {
            return inicio;
        }
        Nodo<T> mitad = mitad(inicio);
        Nodo<T> segunda = mitad.siguiente;
        mitad.siguiente = null;
        return mezclar(ordenarRecursivo(inicio, c), ordenarRecursivo(segunda, c), c);
    }

    private static <T> Nodo<T> mitad(Nodo<T> inicio) {
        Nodo<T> lento = inicio;
        Nodo<T> rapido = inicio.siguiente;
        while (rapido != null && rapido.siguiente != null) {
            lento = lento.siguiente;
            rapido = rapido.siguiente.siguiente;
        }
        return lento;
    }

    private static <T> Nodo<T> mezclar(Nodo<T> a, Nodo<T> b, Comparator<? super T> c) {
        Nodo<T> centinela = new Nodo<>(null);
        Nodo<T> fin = centinela;
        while (a != null && b != null) {
            if (c.compare(a.dato, b.dato) <= 0) {
                fin.siguiente = a;
                a = a.siguiente;
            } else {
                fin.siguiente = b;
                b = b.siguiente;
            }
            fin = fin.siguiente;
        }
        fin.siguiente = (a != null) ? a : b;
        return centinela.siguiente;
    }

    // ------------------------------------------------------------ conversión

    public T[] aArreglo(IntFunction<T[]> generador) {
        T[] arreglo = generador.apply(tamanio);
        int i = 0;
        for (Nodo<T> n = cabeza; n != null; n = n.siguiente) {
            arreglo[i++] = n.dato;
        }
        return arreglo;
    }

    @SafeVarargs
    public static <E> ListaEnlazada<E> de(E... elementos) {
        ListaEnlazada<E> lista = new ListaEnlazada<>();
        for (E e : elementos) {
            lista.agregarAlFinal(e);
        }
        return lista;
    }

    // ------------------------------------------------------------ iteración

    @Override
    public Iterator<T> iterator() {
        return new IteradorLista<>(cabeza, true);
    }

    public Iterable<T> enOrdenInverso() {
        return () -> new IteradorLista<>(cola, false);
    }

    private static final class IteradorLista<T> implements Iterator<T> {
        private Nodo<T> actual;
        private final boolean haciaAdelante;

        private IteradorLista(Nodo<T> inicio, boolean haciaAdelante) {
            this.actual = inicio;
            this.haciaAdelante = haciaAdelante;
        }

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
            actual = haciaAdelante ? actual.siguiente : actual.anterior;
            return dato;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Nodo<T> n = cabeza; n != null; n = n.siguiente) {
            sb.append(n.dato);
            if (n.siguiente != null) {
                sb.append(", ");
            }
        }
        return sb.append(']').toString();
    }

    // ------------------------------------------------------------- auxiliares

    private Nodo<T> nodoEn(int indice) {
        if (indice < tamanio / 2) {
            Nodo<T> n = cabeza;
            for (int i = 0; i < indice; i++) {
                n = n.siguiente;
            }
            return n;
        }
        Nodo<T> n = cola;
        for (int i = tamanio - 1; i > indice; i--) {
            n = n.anterior;
        }
        return n;
    }

    private T desenlazar(Nodo<T> nodo) {
        if (nodo.anterior == null) {
            cabeza = nodo.siguiente;
        } else {
            nodo.anterior.siguiente = nodo.siguiente;
        }
        if (nodo.siguiente == null) {
            cola = nodo.anterior;
        } else {
            nodo.siguiente.anterior = nodo.anterior;
        }
        nodo.anterior = null;
        nodo.siguiente = null;
        tamanio--;
        return nodo.dato;
    }

    private void verificarIndice(int indice) {
        if (indice < 0 || indice >= tamanio) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
    }

    private void exigirNoVacia() {
        if (tamanio == 0) {
            throw new EstructuraVaciaException("La lista está vacía.");
        }
    }

    private static <E> E validar(E elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("La lista no admite elementos nulos.");
        }
        return elemento;
    }
}
