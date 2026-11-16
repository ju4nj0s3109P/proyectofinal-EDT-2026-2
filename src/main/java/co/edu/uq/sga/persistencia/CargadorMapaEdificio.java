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
    public MapaCargado cargar(
            Path archivoUbicaciones,
            Path archivoPasillos
    ) throws IOException {

        Grafo<String> grafo = new Grafo<>(false);
        ListaEnlazada<Ubicacion> ubicaciones = new ListaEnlazada<>();

        var filasUbicaciones = leerTablaCsv(
                archivoUbicaciones,
                new String[]{"id", "tipo", "nombre"}
        );

        int registro = 0;
        int recepciones = 0;

        for (String[] fila : filasUbicaciones) {
            registro++;

            if (registro == 1) {
                continue;
            }

            try {
                String[] campos = validarCamposCsv(fila, 3);

                switch (campos[1]) {
                    case "RECEPCION", "CUARTO_ASEO",
                         "ESCALERA", "APARTAMENTO" -> {
                        // Tipos admitidos para las ubicaciones del edificio.
                    }
                    default -> throw new IllegalArgumentException(
                            "Tipo de ubicación desconocido: " + campos[1]
                    );
                }

                if (!grafo.agregarVertice(campos[0])) {
                    throw new IllegalArgumentException(
                            "Ubicación repetida: " + campos[0]
                    );
                }

                ubicaciones.agregarAlFinal(
                        new Ubicacion(campos[0], campos[1], campos[2])
                );

                if ("RECEPCION".equals(campos[1])) {
                    recepciones++;
                }

            } catch (IllegalArgumentException e) {
                throw errorCsv(archivoUbicaciones, registro - 1, e);
            }
        }

        if (recepciones != 1) {
            throw new IOException(
                    archivoUbicaciones
                            + ": debe existir exactamente una recepción."
            );
        }

        var filasPasillos = leerTablaCsv(
                archivoPasillos,
                new String[]{"origen", "destino", "metros"}
        );

        registro = 0;

        for (String[] fila : filasPasillos) {
            registro++;

            if (registro == 1) {
                continue;
            }

            try {
                String[] campos = validarCamposCsv(fila, 3);

                String origen = campos[0];
                String destino = campos[1];
                double metros = Double.parseDouble(campos[2]);

                if (!grafo.existeVertice(origen)
                        || !grafo.existeVertice(destino)) {
                    throw new IllegalArgumentException(
                            "El pasillo referencia una ubicación inexistente: "
                                    + origen + " - " + destino
                    );
                }

                if (grafo.pesoArista(origen, destino).isPresent()) {
                    throw new IllegalArgumentException(
                            "Pasillo repetido: " + origen + " - " + destino
                    );
                }

                grafo.agregarArista(origen, destino, metros);

            } catch (IllegalArgumentException e) {
                throw errorCsv(archivoPasillos, registro - 1, e);
            }
        }

        return new MapaCargado(grafo, ubicaciones);
    }

    private ListaEnlazada<String[]> leerTablaCsv(
            Path archivo,
            String[] encabezadosEsperados
    ) throws IOException {

        var filas = new LectorCSV().leer(archivo);

        if (filas.estaVacia()) {
            throw new IOException("El archivo CSV está vacío: " + archivo);
        }

        String[] encabezados = filas.primero();

        if (encabezados.length != encabezadosEsperados.length) {
            throw new IOException(
                    archivo + ": cantidad de columnas incorrecta."
            );
        }

        for (int i = 0; i < encabezadosEsperados.length; i++) {
            if (!encabezadosEsperados[i].equals(encabezados[i].trim())) {
                throw new IOException(
                        archivo + ": en la columna " + (i + 1)
                                + " se esperaba " + encabezadosEsperados[i]
                );
            }
        }

        return filas;
    }

    private String[] validarCamposCsv(String[] fila, int cantidad) {
        if (fila.length != cantidad) {
            throw new IllegalArgumentException(
                    "Se esperaban " + cantidad + " columnas."
            );
        }

        String[] campos = fila.clone();

        for (int i = 0; i < campos.length; i++) {
            campos[i] = campos[i].trim();

            if (campos[i].isEmpty()) {
                throw new IllegalArgumentException(
                        "La columna " + (i + 1) + " está vacía."
                );
            }
        }

        return campos;
    }

    private IOException errorCsv(
            Path archivo,
            int registro,
            IllegalArgumentException causa
    ) {
        return new IOException(
                archivo + ", registro de datos " + registro
                        + ": " + causa.getMessage(),
                causa
        );
    }
}