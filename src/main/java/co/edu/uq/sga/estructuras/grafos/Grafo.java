package co.edu.uq.sga.estructuras.grafos;

import co.edu.uq.sga.estructuras.lineales.Cola;
import co.edu.uq.sga.estructuras.lineales.ColaPrioridad;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import java.util.Iterator;
import java.util.Optional;

public class Grafo<T> implements Iterable<T> {

    static final class Vertice<T> {
        final T dato;
        final ListaEnlazada<Arista<T>> adyacentes = new ListaEnlazada<>();
        int indice;

        Vertice(T dato) {
            this.dato = dato;
        }
    }

    public static final class Arista<T> {
        private final Vertice<T> origen;
        private final Vertice<T> destino;
        private double peso;
        private boolean abierta = true;

        private Arista(Vertice<T> origen, Vertice<T> destino, double peso) {
            this.origen = origen;
            this.destino = destino;
            this.peso = peso;
        }

        public T getOrigen() {
            return origen.dato;
        }

        public T getDestino() {
            return destino.dato;
        }

        public double getPeso() {
            return peso;
        }

        public boolean estaAbierta() {
            return abierta;
        }

        Vertice<T> otroExtremo(Vertice<T> desde) {
            return desde == origen ? destino : origen;
        }

        boolean une(Vertice<T> a, Vertice<T> b, boolean dirigido) {
            if (origen == a && destino == b) {
                return true;
            }
            return !dirigido && origen == b && destino == a;
        }

        @Override
        public String toString() {
            return origen.dato + " - " + destino.dato + " (" + peso + (abierta ? ")" : ", cerrada)");
        }
    }

    private final ListaEnlazada<Vertice<T>> vertices = new ListaEnlazada<>();
    private final boolean dirigido;
    private int cantidadAristas;

    public Grafo() {
        this(false);
    }

    public Grafo(boolean dirigido) {
        this.dirigido = dirigido;
    }

    public boolean esDirigido() {
        return dirigido;
    }

    public boolean agregarVertice(T dato) {
        validar(dato);
        if (buscarVertice(dato) != null) {
            return false;
        }
        vertices.agregarAlFinal(new Vertice<>(dato));
        return true;
    }

    public boolean existeVertice(T dato) {
        return dato != null && buscarVertice(dato) != null;
    }

    public boolean eliminarVertice(T dato) {
        Vertice<T> vertice = buscarVertice(dato);
        if (vertice == null) {
            return false;
        }
        cantidadAristas -= vertice.adyacentes.tamanio();
        for (Vertice<T> v : vertices) {
            if (v != vertice) {
                int quitadas = v.adyacentes.eliminarSi(a -> a.otroExtremo(v) == vertice);
                if (dirigido) {
                    cantidadAristas -= quitadas;
                }
            }
        }
        vertices.eliminar(vertice);
        return true;
    }

    public void agregarArista(T origen, T destino, double peso) {
        Vertice<T> a = exigirVertice(origen);
        Vertice<T> b = exigirVertice(destino);
        if (a == b) {
            throw new IllegalArgumentException("No se permiten pasillos de un punto a sí mismo.");
        }
        if (!(peso > 0) || Double.isInfinite(peso)) {
            throw new IllegalArgumentException("El peso debe ser un número positivo.");
        }
        Arista<T> existente = buscarArista(a, b);
        if (existente != null) {
            existente.peso = peso;
            return;
        }
        Arista<T> arista = new Arista<>(a, b, peso);
        a.adyacentes.agregarAlFinal(arista);
        if (!dirigido) {
            b.adyacentes.agregarAlFinal(arista);
        }
        cantidadAristas++;
    }

    public boolean eliminarArista(T origen, T destino) {
        Vertice<T> a = buscarVertice(origen);
        Vertice<T> b = buscarVertice(destino);
        Arista<T> arista = (a == null || b == null) ? null : buscarArista(a, b);
        if (arista == null) {
            return false;
        }
        a.adyacentes.eliminar(arista);
        if (!dirigido) {
            b.adyacentes.eliminar(arista);
        }
        cantidadAristas--;
        return true;
    }

    public boolean cerrarArista(T origen, T destino) {
        return cambiarEstadoArista(origen, destino, false);
    }

    public boolean abrirArista(T origen, T destino) {
        return cambiarEstadoArista(origen, destino, true);
    }

    public Optional<Double> pesoArista(T origen, T destino) {
        Vertice<T> a = buscarVertice(origen);
        Vertice<T> b = buscarVertice(destino);
        Arista<T> arista = (a == null || b == null) ? null : buscarArista(a, b);
        return arista == null ? Optional.empty() : Optional.of(arista.peso);
    }

    public ListaEnlazada<T> vecinos(T dato) {
        Vertice<T> v = exigirVertice(dato);
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        for (Arista<T> a : v.adyacentes) {
            if (a.abierta) {
                resultado.agregarAlFinal(a.otroExtremo(v).dato);
            }
        }
        return resultado;
    }

    public ListaEnlazada<T> vertices() {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        for (Vertice<T> v : vertices) {
            resultado.agregarAlFinal(v.dato);
        }
        return resultado;
    }

    public ListaEnlazada<Arista<T>> aristas() {
        ListaEnlazada<Arista<T>> resultado = new ListaEnlazada<>();
        for (Vertice<T> v : vertices) {
            for (Arista<T> a : v.adyacentes) {
                if (a.origen == v) {
                    resultado.agregarAlFinal(a);
                }
            }
        }
        return resultado;
    }

    public int cantidadVertices() {
        return vertices.tamanio();
    }

    public int cantidadAristas() {
        return cantidadAristas;
    }

    public ListaEnlazada<T> recorridoAnchura(T origen) {
        Vertice<T> inicio = exigirVertice(origen);
        boolean[] visitado = new boolean[numerarVertices()];
        ListaEnlazada<T> orden = new ListaEnlazada<>();
        Cola<Vertice<T>> cola = new Cola<>();
        visitado[inicio.indice] = true;
        cola.encolar(inicio);
        while (!cola.estaVacia()) {
            Vertice<T> actual = cola.desencolar();
            orden.agregarAlFinal(actual.dato);
            for (Arista<T> a : actual.adyacentes) {
                Vertice<T> vecino = a.otroExtremo(actual);
                if (a.abierta && !visitado[vecino.indice]) {
                    visitado[vecino.indice] = true;
                    cola.encolar(vecino);
                }
            }
        }
        return orden;
    }

    public ListaEnlazada<T> recorridoProfundidad(T origen) {
        Vertice<T> inicio = exigirVertice(origen);
        boolean[] visitado = new boolean[numerarVertices()];
        ListaEnlazada<T> orden = new ListaEnlazada<>();
        profundidad(inicio, visitado, orden);
        return orden;
    }

    private void profundidad(Vertice<T> actual, boolean[] visitado, ListaEnlazada<T> orden) {
        visitado[actual.indice] = true;
        orden.agregarAlFinal(actual.dato);
        for (Arista<T> a : actual.adyacentes) {
            Vertice<T> vecino = a.otroExtremo(actual);
            if (a.abierta && !visitado[vecino.indice]) {
                profundidad(vecino, visitado, orden);
            }
        }
    }

    private static final class Etiqueta<T> {
        private final Vertice<T> vertice;
        private final double distancia;

        private Etiqueta(Vertice<T> vertice, double distancia) {
            this.vertice = vertice;
            this.distancia = distancia;
        }
    }

    public Ruta<T> dijkstra(T origen, T destino) {
        Vertice<T> inicio = exigirVertice(origen);
        Vertice<T> fin = exigirVertice(destino);
        int n = numerarVertices();
        double[] distancia = new double[n];
        @SuppressWarnings("unchecked")
        Vertice<T>[] predecesor = (Vertice<T>[]) new Vertice<?>[n];
        boolean[] definitivo = new boolean[n];
        for (int i = 0; i < n; i++) {
            distancia[i] = Double.POSITIVE_INFINITY;
        }

        ColaPrioridad<Etiqueta<T>> frontera =
                new ColaPrioridad<>((x, y) -> Double.compare(x.distancia, y.distancia));
        distancia[inicio.indice] = 0;
        frontera.insertar(new Etiqueta<>(inicio, 0));

        while (!frontera.estaVacia()) {
            Vertice<T> actual = frontera.extraerPrimero().vertice;
            if (definitivo[actual.indice]) {
                continue;
            }
            definitivo[actual.indice] = true;
            if (actual == fin) {
                break;
            }
            for (Arista<T> a : actual.adyacentes) {
                Vertice<T> vecino = a.otroExtremo(actual);
                if (!a.abierta || definitivo[vecino.indice]) {
                    continue;
                }
                double candidata = distancia[actual.indice] + a.peso;
                if (candidata < distancia[vecino.indice]) {
                    distancia[vecino.indice] = candidata;
                    predecesor[vecino.indice] = actual;
                    frontera.insertar(new Etiqueta<>(vecino, candidata));
                }
            }
        }

        if (Double.isInfinite(distancia[fin.indice])) {
            return Ruta.vacia();
        }
        return new Ruta<>(reconstruir(fin, predecesor), distancia[fin.indice]);
    }

    public Ruta<T> rutaMenosTramos(T origen, T destino) {
        Vertice<T> inicio = exigirVertice(origen);
        Vertice<T> fin = exigirVertice(destino);
        int n = numerarVertices();
        boolean[] visitado = new boolean[n];
        double[] costo = new double[n];
        @SuppressWarnings("unchecked")
        Vertice<T>[] predecesor = (Vertice<T>[]) new Vertice<?>[n];
        Cola<Vertice<T>> cola = new Cola<>();
        visitado[inicio.indice] = true;
        cola.encolar(inicio);
        while (!cola.estaVacia()) {
            Vertice<T> actual = cola.desencolar();
            if (actual == fin) {
                return new Ruta<>(reconstruir(fin, predecesor), costo[fin.indice]);
            }
            for (Arista<T> a : actual.adyacentes) {
                Vertice<T> vecino = a.otroExtremo(actual);
                if (a.abierta && !visitado[vecino.indice]) {
                    visitado[vecino.indice] = true;
                    predecesor[vecino.indice] = actual;
                    costo[vecino.indice] = costo[actual.indice] + a.peso;
                    cola.encolar(vecino);
                }
            }
        }
        return Ruta.vacia();
    }

    public Ruta<T> rutaPorParadas(T origen, ListaEnlazada<T> paradas) {
        exigirVertice(origen);
        if (paradas == null) {
            throw new IllegalArgumentException("Las paradas son obligatorias.");
        }
        ListaEnlazada<T> camino = new ListaEnlazada<>();
        camino.agregarAlFinal(origen);
        double total = 0;
        T desde = origen;
        for (T parada : paradas) {
            if (parada.equals(desde)) {
                continue;
            }
            Ruta<T> tramo = dijkstra(desde, parada);
            if (!tramo.existe()) {
                return Ruta.vacia();
            }
            ListaEnlazada<T> pasos = tramo.getCamino();
            pasos.eliminarPrimero();
            for (T paso : pasos) {
                camino.agregarAlFinal(paso);
            }
            total += tramo.getCosto();
            desde = parada;
        }
        return new Ruta<>(camino, total);
    }

    private static <T> ListaEnlazada<T> reconstruir(Vertice<T> fin, Vertice<T>[] predecesor) {
        ListaEnlazada<T> camino = new ListaEnlazada<>();
        for (Vertice<T> v = fin; v != null; v = predecesor[v.indice]) {
            camino.agregarAlInicio(v.dato);
        }
        return camino;
    }

    public ListaEnlazada<T> alcanzablesDesde(T origen) {
        return recorridoProfundidad(origen);
    }

    public ListaEnlazada<T> inalcanzablesDesde(T origen) {
        Vertice<T> inicio = exigirVertice(origen);
        boolean[] visitado = new boolean[numerarVertices()];
        profundidad(inicio, visitado, new ListaEnlazada<>());
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        for (Vertice<T> v : vertices) {
            if (!visitado[v.indice]) {
                resultado.agregarAlFinal(v.dato);
            }
        }
        return resultado;
    }

    public ListaEnlazada<ListaEnlazada<T>> componentesConexas() {
        boolean[] visitado = new boolean[numerarVertices()];
        ListaEnlazada<ListaEnlazada<T>> componentes = new ListaEnlazada<>();
        for (Vertice<T> v : vertices) {
            if (!visitado[v.indice]) {
                ListaEnlazada<T> componente = new ListaEnlazada<>();
                profundidad(v, visitado, componente);
                componentes.agregarAlFinal(componente);
            }
        }
        return componentes;
    }

    public boolean esConexo() {
        return vertices.estaVacia() || inalcanzablesDesde(vertices.primero().dato).estaVacia();
    }

    @Override
    public Iterator<T> iterator() {
        return vertices().iterator();
    }

    int numerarVertices() {
        int i = 0;
        for (Vertice<T> v : vertices) {
            v.indice = i++;
        }
        return i;
    }

    Vertice<T> buscarVertice(T dato) {
        if (dato == null) {
            return null;
        }
        return vertices.buscar(v -> v.dato.equals(dato)).orElse(null);
    }

    Vertice<T> exigirVertice(T dato) {
        validar(dato);
        Vertice<T> v = buscarVertice(dato);
        if (v == null) {
            throw new IllegalArgumentException("No existe el vértice: " + dato);
        }
        return v;
    }

    private Arista<T> buscarArista(Vertice<T> a, Vertice<T> b) {
        return a.adyacentes.buscar(arista -> arista.une(a, b, dirigido)).orElse(null);
    }

    private boolean cambiarEstadoArista(T origen, T destino, boolean abierta) {
        Vertice<T> a = buscarVertice(origen);
        Vertice<T> b = buscarVertice(destino);
        Arista<T> arista = (a == null || b == null) ? null : buscarArista(a, b);
        if (arista == null) {
            return false;
        }
        arista.abierta = abierta;
        return true;
    }

    private static void validar(Object dato) {
        if (dato == null) {
            throw new IllegalArgumentException("El vértice no puede ser nulo.");
        }
    }
}
