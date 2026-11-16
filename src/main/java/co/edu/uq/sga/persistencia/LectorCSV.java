package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;

import java.io.IOException;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LectorCSV {

    public ListaEnlazada<String[]> leer(Path archivo) throws IOException {
        ListaEnlazada<String[]> filas = new ListaEnlazada<>();
        ListaEnlazada<String> campos = new ListaEnlazada<>();
        StringBuilder campo = new StringBuilder();

        boolean entreComillas = false;
        boolean comillasCerradas = false;
        boolean filaIniciada = false;
        boolean primerCaracter = true;
        int numeroRegistro = 1;

        try (PushbackReader lector = new PushbackReader(
                Files.newBufferedReader(archivo, StandardCharsets.UTF_8), 1)) {

            int valor;

            while ((valor = lector.read()) != -1) {
                char caracter = (char) valor;

                // Algunos editores añaden esta marca al inicio del UTF-8.
                if (primerCaracter) {
                    primerCaracter = false;

                    if (caracter == '\uFEFF') {
                        continue;
                    }
                }

                if (entreComillas) {
                    if (caracter == '"') {
                        int siguiente = lector.read();

                        if (siguiente == '"') {
                            campo.append('"');
                        } else {
                            entreComillas = false;
                            comillasCerradas = true;

                            if (siguiente != -1) {
                                lector.unread(siguiente);
                            }
                        }
                    } else {
                        campo.append(caracter);
                    }

                    continue;
                }

                if (comillasCerradas
                        && caracter != ';'
                        && caracter != '\n'
                        && caracter != '\r') {
                    throw error(numeroRegistro,
                            "Después de cerrar comillas se esperaba un separador "
                                    + "o un salto de línea.");
                }

                if (caracter == '"') {
                    if (campo.length() != 0) {
                        throw error(numeroRegistro,
                                "Las comillas deben comenzar al inicio del campo.");
                    }

                    entreComillas = true;
                    filaIniciada = true;

                } else if (caracter == ';') {
                    campos.agregarAlFinal(campo.toString());
                    campo.setLength(0);
                    comillasCerradas = false;
                    filaIniciada = true;

                } else if (caracter == '\n' || caracter == '\r') {
                    if (caracter == '\r') {
                        int siguiente = lector.read();

                        if (siguiente != '\n' && siguiente != -1) {
                            lector.unread(siguiente);
                        }
                    }

                    if (filaIniciada) {
                        campos.agregarAlFinal(campo.toString());
                        filas.agregarAlFinal(campos.aArreglo(String[]::new));
                    }

                    campos = new ListaEnlazada<>();
                    campo.setLength(0);
                    comillasCerradas = false;
                    filaIniciada = false;
                    numeroRegistro++;

                } else {
                    campo.append(caracter);
                    filaIniciada = true;
                }
            }

            if (entreComillas) {
                throw error(numeroRegistro,
                        "Hay un campo con comillas sin cerrar.");
            }

            if (filaIniciada) {
                campos.agregarAlFinal(campo.toString());
                filas.agregarAlFinal(campos.aArreglo(String[]::new));
            }
        }

        return filas;
    }

    private IOException error(int registro, String detalle) {
        return new IOException(
                "CSV inválido en el registro " + registro + ": " + detalle
        );
    }
}