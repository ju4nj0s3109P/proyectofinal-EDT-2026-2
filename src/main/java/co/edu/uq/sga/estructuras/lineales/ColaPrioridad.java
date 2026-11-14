package co.edu.uq.sga.estructuras.lineales;

import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

public class ColaPrioridad<T> implements Iterable<T> {

    private static final int CAPACIDAD_INICIAL = 8;

    private static final class Entrada<T> {
        private final T dato;
        private final long secuencia;

        private Entrada(T dato, long secuencia) {
            this.dato = dato;
            this.secuencia = secuencia;
        }
    }

    private final Comparator<? super T> comparador;
    private Entrada<T>[] monticulo;
    private int tamanio;
    private long siguienteSecuencia;

    public ColaPrioridad(Comparator<? super T> comparador) {
        this.comparador = Objects.requireNonNull(comparador, "El comparador es obligatorio.");
        this.monticulo = crearArreglo(CAPACIDAD_INICIAL);
    }

    public static <E extends Comparable<? super E>> ColaPrioridad<E> ordenNatural() {
        return new ColaPrioridad<>(Comparator.naturalOrder());
    }

    public void insertar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("La cola de prioridad no admite elementos nulos.");
        }
        if (tamanio == monticulo.length) {
            crecer();
        }
        monticulo[tamanio] = new Entrada<>(elemento, siguienteSecuencia++);
        flotar(tamanio);
        tamanio++;
    }

    public T verPrimero() {
        exigirNoVacia();
        return monticulo[0].dato;
    }

    public T extraerPrimero() {
        exigirNoVacia();
        return quitarEn(0);
    }

    public boolean contiene(T elemento) {
        return posicionDe(elemento) >= 0;
    }

    public boolean eliminar(T elemento) {
        int posicion = posicionDe(elemento);
        if (posicion < 0) {
            return false;
        }
        quitarEn(posicion);
        return true;
    }

    public int tamanio() {
        return tamanio;
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public void limpiar() {
        monticulo = crearArreglo(CAPACIDAD_INICIAL);
        tamanio = 0;
    }

    public ListaEnlazada<T> enOrden() {
        ListaEnlazada<T> lista = new ListaEnlazada<>();
        for (T elemento : this) {
            lista.agregarAlFinal(elemento);
        }
        return lista;
    }

    @Override
    public Iterator<T> iterator() {
        ColaPrioridad<T> copia = new ColaPrioridad<>(comparador);
        copia.monticulo = monticulo.clone();
        copia.tamanio = tamanio;
        copia.siguienteSecuencia = siguienteSecuencia;
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !copia.estaVacia();
            }

            @Override
            public T next() {
                return copia.extraerPrimero();
            }
        };
    }

    private static int padre(int i) {
        return (i - 1) / 2;
    }

    private void flotar(int i) {
        if (i == 0) {
            return;
        }
        int p = padre(i);
        if (menor(i, p)) {
            intercambiar(i, p);
            flotar(p);
        }
    }

    private void hundir(int i) {
        int izquierdo = 2 * i + 1;
        int derecho = izquierdo + 1;
        int menorPos = i;
        if (izquierdo < tamanio && menor(izquierdo, menorPos)) {
            menorPos = izquierdo;
        }
        if (derecho < tamanio && menor(derecho, menorPos)) {
            menorPos = derecho;
        }
        if (menorPos != i) {
            intercambiar(i, menorPos);
            hundir(menorPos);
        }
    }

    private T quitarEn(int posicion) {
        T dato = monticulo[posicion].dato;
        tamanio--;
        if (posicion != tamanio) {
            monticulo[posicion] = monticulo[tamanio];
            monticulo[tamanio] = null;
            hundir(posicion);
            flotar(posicion);
        } else {
            monticulo[tamanio] = null;
        }
        return dato;
    }

    private boolean menor(int a, int b) {
        int resultado = comparador.compare(monticulo[a].dato, monticulo[b].dato);
        if (resultado != 0) {
            return resultado < 0;
        }
        return monticulo[a].secuencia < monticulo[b].secuencia;
    }

    private void intercambiar(int a, int b) {
        Entrada<T> temporal = monticulo[a];
        monticulo[a] = monticulo[b];
        monticulo[b] = temporal;
    }

    private int posicionDe(T elemento) {
        if (elemento == null) {
            return -1;
        }
        for (int i = 0; i < tamanio; i++) {
            if (monticulo[i].dato.equals(elemento)) {
                return i;
            }
        }
        return -1;
    }

    private void crecer() {
        Entrada<T>[] nuevo = crearArreglo(monticulo.length * 2);
        for (int i = 0; i < tamanio; i++) {
            nuevo[i] = monticulo[i];
        }
        monticulo = nuevo;
    }

    @SuppressWarnings("unchecked")
    private static <T> Entrada<T>[] crearArreglo(int capacidad) {
        return (Entrada<T>[]) new Entrada<?>[capacidad];
    }

    private void exigirNoVacia() {
        if (tamanio == 0) {
            throw new EstructuraVaciaException("La cola de prioridad está vacía.");
        }
    }
}
