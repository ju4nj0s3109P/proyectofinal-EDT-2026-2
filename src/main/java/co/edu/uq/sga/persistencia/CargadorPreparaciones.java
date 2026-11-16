package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;

import java.io.IOException;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class CargadorPreparaciones {

    public record DatosPreparacion(
            String apartamento,
            LocalDateTime llegada
    ) {}

    public DatosPreparacion[] cargar(
            Path archivo,
            LocalDate fechaOperacion,
            CatalogoApartamentosArbol catalogo
    ) throws IOException {

        var filas = new LectorCSV().leer(archivo);

        if (filas.estaVacia()) {
            throw new IOException("El CSV de preparaciones está vacío.");
        }

        String[] esperados = {
                "apartamento",
                "diasDesdeFechaOperacion",
                "horaLlegada"
        };

        String[] encabezados = filas.primero();

        if (encabezados.length != esperados.length) {
            throw new IOException(
                    "El encabezado de preparaciones debe tener tres columnas."
            );
        }

        for (int i = 0; i < esperados.length; i++) {
            if (!esperados[i].equals(encabezados[i].trim())) {
                throw new IOException(
                        "En la columna " + (i + 1)
                                + " se esperaba " + esperados[i]
                );
            }
        }

        ListaEnlazada<DatosPreparacion> tareas = new ListaEnlazada<>();
        int registro = 0;

        for (String[] fila : filas) {
            registro++;

            if (registro == 1) {
                continue;
            }

            try {
                if (fila.length != 3) {
                    throw new IllegalArgumentException(
                            "Se esperaban tres columnas."
                    );
                }

                String codigo = fila[0].trim();
                int dias = Integer.parseInt(fila[1].trim());
                LocalTime hora = LocalTime.parse(fila[2].trim());

                if (dias < 0) {
                    throw new IllegalArgumentException(
                            "Los días desde la fecha de operación no pueden ser negativos."
                    );
                }

                var apartamento = catalogo.buscarPorCodigo(codigo)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Apartamento inexistente: " + codigo
                        ));

                if (apartamento.getEstadoOperativo()
                        != EstadoApartamento.PENDIENTE_PREPARACION) {
                    throw new IllegalArgumentException(
                            codigo + " no está pendiente de preparación."
                    );
                }

                if (tareas.existe(tarea ->
                        tarea.apartamento().equals(codigo))) {
                    throw new IllegalArgumentException(
                            "Tarea duplicada para " + codigo
                    );
                }

                LocalDateTime llegada =
                        fechaOperacion.plusDays(dias).atTime(hora);

                tareas.agregarAlFinal(
                        new DatosPreparacion(codigo, llegada)
                );

            } catch (IllegalArgumentException | DateTimeException e) {
                throw new IOException(
                        archivo + ", registro de datos " + (registro - 1)
                                + ": " + e.getMessage(),
                        e
                );
            }
        }

        return tareas.aArreglo(DatosPreparacion[]::new);
    }
}