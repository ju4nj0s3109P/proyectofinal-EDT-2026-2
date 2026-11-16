package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.dominio.enums.TipoTemporada;
import co.edu.uq.sga.dominio.modelo.Temporada;
import co.edu.uq.sga.servicios.ServicioTarifas;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class CargadorTemporadas {

    public ServicioTarifas cargar(Path archivo) throws IOException {
        ServicioTarifas servicio = new ServicioTarifas();
        int registro = 0;
        int cantidad = 0;

        for (String[] campos : new LectorCSV().leer(archivo)) {
            registro++;

            if (registro == 1) {
                if (campos.length != 3
                        || !"tipo".equals(campos[0].trim())
                        || !"inicio".equals(campos[1].trim())
                        || !"fin".equals(campos[2].trim())) {
                    throw new IOException(
                            "El encabezado debe ser: tipo;inicio;fin"
                    );
                }
                continue;
            }

            if (campos.length != 3) {
                throw new IOException(
                        "Registro " + registro
                                + ": se requieren tipo, inicio y fin."
                );
            }

            try {
                Temporada temporada = new Temporada(
                        TipoTemporada.valueOf(campos[0].trim()),
                        LocalDate.parse(campos[1].trim()),
                        LocalDate.parse(campos[2].trim())
                );

                servicio.definirTemporada(temporada);
                cantidad++;

            } catch (IllegalArgumentException
                     | IllegalStateException
                     | DateTimeParseException e) {
                throw new IOException(
                        "Temporada inválida en el registro "
                                + registro + ": " + e.getMessage(),
                        e
                );
            }
        }

        if (cantidad == 0) {
            throw new IOException(
                    "El archivo debe contener al menos una temporada."
            );
        }

        return servicio;
    }
}