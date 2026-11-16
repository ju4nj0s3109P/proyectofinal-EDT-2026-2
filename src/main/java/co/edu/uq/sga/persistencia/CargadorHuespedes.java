package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.adaptadores.CatalogoHuespedesArbol;
import co.edu.uq.sga.dominio.modelo.Huesped;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class CargadorHuespedes {

    private static final String[] ENCABEZADOS = {
            "documento",
            "nombre",
            "fechaNacimiento",
            "telefono",
            "correo",
            "fotografia"
    };

    public CatalogoHuespedesArbol cargar(Path archivo)
            throws IOException {

        var filas = new LectorCSV().leer(archivo);

        if (filas.estaVacia()) {
            throw new IOException(
                    "El archivo de huéspedes está vacío: " + archivo
            );
        }

        validarEncabezados(filas.primero());

        CatalogoHuespedesArbol catalogo = new CatalogoHuespedesArbol();
        int registro = 0;

        for (String[] fila : filas) {
            registro++;

            if (registro == 1) {
                continue;
            }

            try {
                if (fila.length != ENCABEZADOS.length) {
                    throw new IllegalArgumentException(
                            "Se esperaban seis columnas."
                    );
                }

                String[] campos = fila.clone();

                for (int i = 0; i < campos.length; i++) {
                    campos[i] = campos[i].trim();
                }

                Huesped huesped = new Huesped(
                        campos[1],
                        campos[0],
                        LocalDate.parse(campos[2]),
                        campos[3],
                        campos[4],
                        campos[5]
                );

                catalogo.agregar(huesped);

            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw new IOException(
                        archivo + ", registro de datos " + (registro - 1)
                                + ": " + e.getMessage(),
                        e
                );
            }
        }

        return catalogo;
    }

    private void validarEncabezados(String[] encabezados)
            throws IOException {

        if (encabezados.length != ENCABEZADOS.length) {
            throw new IOException(
                    "El encabezado de huéspedes debe tener seis columnas."
            );
        }

        for (int i = 0; i < ENCABEZADOS.length; i++) {
            if (!ENCABEZADOS[i].equals(encabezados[i].trim())) {
                throw new IOException(
                        "En la columna " + (i + 1)
                                + " se esperaba " + ENCABEZADOS[i]
                );
            }
        }
    }
}