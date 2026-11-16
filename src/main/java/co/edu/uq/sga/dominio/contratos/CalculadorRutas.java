package co.edu.uq.sga.dominio.contratos;

import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;

/** Puerto hacia el grafo de ubicaciones del alojamiento. */
public interface CalculadorRutas {
    /** Devuelve una ruta con origen y destino incluidos, o vacío si no existe ruta. */
    String[] calcularRuta(String origen, String destino);

    /**
     * Construye un recorrido que visita los destinos en el orden recibido,
     * concatenando rutas mínimas entre cada par consecutivo.
     */
    default String[] calcularRecorrido(String origen, String[] destinos) {
        if (origen == null || origen.isBlank() || destinos == null) return new String[0];
        ListaEnlazada<String> recorrido = new ListaEnlazada<>();
        String actual = origen.trim();
        recorrido.agregarAlFinal(actual);
        for (String destino : destinos) {
            if (destino == null || destino.isBlank()) return new String[0];
            String siguiente = destino.trim();
            if (actual.equals(siguiente)) continue;
            String[] tramo = calcularRuta(actual, siguiente);
            if (tramo == null || tramo.length == 0 || !actual.equals(tramo[0])
                    || !siguiente.equals(tramo[tramo.length - 1])) return new String[0];
            for (int i = 1; i < tramo.length; i++) recorrido.agregarAlFinal(tramo[i]);
            actual = siguiente;
        }
        return recorrido.aArreglo(String[]::new);
    }
}
