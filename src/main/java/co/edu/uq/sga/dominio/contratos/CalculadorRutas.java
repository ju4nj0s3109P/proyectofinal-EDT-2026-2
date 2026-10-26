package co.edu.uq.sga.dominio.contratos;

/** Puerto hacia el grafo de ubicaciones del alojamiento. */
public interface CalculadorRutas {
    /**
     * Devuelve una ruta con origen y destino incluidos, o un arreglo vacío si
     * no existe ruta.
     */
    String[] calcularRuta(String origen, String destino);
}
