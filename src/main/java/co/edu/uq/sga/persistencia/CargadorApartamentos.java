package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.modelo.Apartamento;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;

public final class CargadorApartamentos {

    private static final String[] ENCABEZADOS = {
            "codigo",
            "nombre",
            "bloque",
            "piso",
            "dormitorios",
            "capacidad",
            "caracteristicas",
            "tarifaBaja",
            "tarifaMedia",
            "tarifaAlta",
            "estado",
            "motivo"
    };

    public CatalogoApartamentosArbol cargar(Path archivo)
            throws IOException {

        var filas = new LectorCSV().leer(archivo);

        if (filas.estaVacia()) {
            throw new IOException(
                    "El archivo CSV está vacío: " + archivo
            );
        }

        validarEncabezados(filas.primero());

        CatalogoApartamentosArbol catalogo =
                new CatalogoApartamentosArbol();

        int numeroRegistro = 0;

        for (String[] campos : filas) {
            numeroRegistro++;

            if (numeroRegistro == 1) {
                continue;
            }

            try {
                Apartamento apartamento = interpretar(campos);
                catalogo.agregar(apartamento);

            } catch (IllegalArgumentException e) {
                throw new IOException(
                        "Error en " + archivo.getFileName()
                                + ", registro de datos "
                                + (numeroRegistro - 1)
                                + ": " + e.getMessage(),
                        e
                );
            }
        }

        if (catalogo.cantidad() == 0) {
            throw new IOException(
                    "El CSV tiene encabezados, pero no contiene apartamentos."
            );
        }

        return catalogo;
    }

    private void validarEncabezados(String[] encabezados)
            throws IOException {

        if (encabezados.length != ENCABEZADOS.length) {
            throw new IOException(
                    "El CSV debe tener " + ENCABEZADOS.length
                            + " columnas en el encabezado."
            );
        }

        for (int i = 0; i < ENCABEZADOS.length; i++) {
            if (!ENCABEZADOS[i].equals(encabezados[i].trim())) {
                throw new IOException(
                        "Encabezado incorrecto en la columna " + (i + 1)
                                + ". Se esperaba: " + ENCABEZADOS[i]
                );
            }
        }
    }

    private Apartamento interpretar(String[] fila) {
        if (fila.length != ENCABEZADOS.length) {
            throw new IllegalArgumentException(
                    "Se esperaban " + ENCABEZADOS.length + " columnas."
            );
        }

        String[] campos = fila.clone();

        for (int i = 0; i < campos.length; i++) {
            campos[i] = campos[i].trim();
        }

        String[] caracteristicas = campos[6].isEmpty()
                ? new String[0]
                : campos[6].split("\\|", -1);

        Apartamento apartamento = new Apartamento(
                campos[0],
                campos[1],
                campos[2],
                Integer.parseInt(campos[3]),
                Integer.parseInt(campos[4]),
                Integer.parseInt(campos[5]),
                caracteristicas,
                new BigDecimal(campos[7]),
                new BigDecimal(campos[8]),
                new BigDecimal(campos[9])
        );

        EstadoApartamento estado =
                EstadoApartamento.valueOf(campos[10]);

        String motivo = campos[11];

        if (estado == EstadoApartamento.FUERA_DE_SERVICIO) {
            apartamento.marcarFueraDeServicio(motivo);
        } else {
            if (!motivo.isEmpty()) {
                throw new IllegalArgumentException(
                        "El motivo solo corresponde al estado FUERA_DE_SERVICIO."
                );
            }

            apartamento.cambiarEstadoOperativo(estado);
        }

        return apartamento;
    }
}