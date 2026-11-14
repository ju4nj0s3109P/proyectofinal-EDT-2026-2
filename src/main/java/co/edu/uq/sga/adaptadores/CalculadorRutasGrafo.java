package co.edu.uq.sga.adaptadores;

import co.edu.uq.sga.dominio.contratos.CalculadorRutas;
import co.edu.uq.sga.estructuras.grafos.Grafo;
import co.edu.uq.sga.estructuras.grafos.Ruta;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;

public class CalculadorRutasGrafo implements CalculadorRutas {

    private final Grafo<String> mapa;

    public CalculadorRutasGrafo(Grafo<String> mapa) {
        if (mapa == null) {
            throw new IllegalArgumentException("El mapa del edificio es obligatorio.");
        }
        this.mapa = mapa;
    }

    @Override
    public String[] calcularRuta(String origen, String destino) {
        if (!mapa.existeVertice(origen) || !mapa.existeVertice(destino)) {
            return new String[0];
        }
        return aArreglo(mapa.dijkstra(origen, destino));
    }

    public String[] calcularRecorrido(String origen, String[] paradas) {
        if (!mapa.existeVertice(origen) || paradas == null) {
            return new String[0];
        }
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        for (String parada : paradas) {
            if (!mapa.existeVertice(parada)) {
                return new String[0];
            }
            lista.agregarAlFinal(parada);
        }
        return aArreglo(mapa.rutaPorParadas(origen, lista));
    }

    public double costoRuta(String origen, String destino) {
        if (!mapa.existeVertice(origen) || !mapa.existeVertice(destino)) {
            return Double.POSITIVE_INFINITY;
        }
        return mapa.dijkstra(origen, destino).getCosto();
    }

    public Grafo<String> getMapa() {
        return mapa;
    }

    private static String[] aArreglo(Ruta<String> ruta) {
        return ruta.existe() ? ruta.getCamino().aArreglo(String[]::new) : new String[0];
    }
}
