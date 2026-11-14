package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.estructuras.grafos.Grafo;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class CargadorMapaEdificio {

    public record Ubicacion(String id, String tipo, String nombre) {}

    public record MapaCargado(Grafo<String> grafo, ListaEnlazada<Ubicacion> ubicaciones) {}

    public MapaCargado cargar(Path archivo) throws IOException {
        Grafo<String> grafo = new Grafo<>(false);
        ListaEnlazada<Ubicacion> ubicaciones = new ListaEnlazada<>();

        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            int numeroLinea = 0;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                linea = linea.strip();

                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }

                try {
                    String[] campos = linea.split("\\|", -1);
                    if (campos.length != 4) {
                        throw new IllegalArgumentException("Se esperan cuatro campos.");
                    }

                    for (int i = 0; i < campos.length; i++) {
                        campos[i] = campos[i].strip();
                        if (campos[i].isEmpty()) {
                            throw new IllegalArgumentException("Hay un campo vacío.");
                        }
                    }

                    switch (campos[0]) {
                        case "NODO" -> {
                            if (!grafo.agregarVertice(campos[1])) {
                                throw new IllegalArgumentException("Nodo repetido: " + campos[1]);
                            }
                            ubicaciones.agregarAlFinal(
                                    new Ubicacion(campos[1], campos[2], campos[3]));
                        }
                        case "PASILLO" ->
                                grafo.agregarArista(campos[1], campos[2], Double.parseDouble(campos[3]));
                        default ->
                                throw new IllegalArgumentException("Registro desconocido: " + campos[0]);
                    }
                } catch (IllegalArgumentException e) {
                    throw new IOException(
                            archivo + ", línea " + numeroLinea + ": " + e.getMessage(), e);
                }
            }
        }

        return new MapaCargado(grafo, ubicaciones);
    }
}