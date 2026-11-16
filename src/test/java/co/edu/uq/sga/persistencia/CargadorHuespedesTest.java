package co.edu.uq.sga.persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CargadorHuespedesTest {

    @TempDir
    Path carpeta;

    private static final String ENCABEZADO =
            "documento;nombre;fechaNacimiento;telefono;correo;fotografia\n";

    @Test
    void cargaYBuscaHuespedPorDocumento() throws IOException {
        Path archivo = escribir(
                "DEMO002;Carlos Ruiz;1988-09-23;3000000002;carlos@example.com;\n"
                        + "DEMO001;Laura Gómez;1995-06-12;3000000001;laura@example.com;\n"
        );

        var catalogo = new CargadorHuespedes().cargar(archivo);
        var laura = catalogo.buscarPorDocumento("DEMO001").orElseThrow();

        assertEquals(2, catalogo.cantidad());
        assertEquals("Laura Gómez", laura.getNombre());
        assertNull(laura.getRutaFotografia());
        assertEquals(
                "DEMO001",
                catalogo.listarPorDocumento()[0].getDocumento()
        );
        assertTrue(catalogo.buscarPorDocumento("NO-EXISTE").isEmpty());
    }

    @Test
    void rechazaDocumentoDuplicado() throws IOException {
        String fila =
                "DEMO001;Laura Gómez;1995-06-12;3000000001;laura@example.com;\n";

        Path archivo = escribir(fila + fila);

        assertThrows(
                IOException.class,
                () -> new CargadorHuespedes().cargar(archivo)
        );
    }

    @Test
    void rechazaFechaInvalida() throws IOException {
        Path archivo = escribir(
                "DEMO001;Laura Gómez;fecha-invalida;3000000001;laura@example.com;\n"
        );

        assertThrows(
                IOException.class,
                () -> new CargadorHuespedes().cargar(archivo)
        );
    }

    @Test
    void rechazaCorreoInvalido() throws IOException {
        Path archivo = escribir(
                "DEMO001;Laura Gómez;1995-06-12;3000000001;correo-invalido;\n"
        );

        assertThrows(
                IOException.class,
                () -> new CargadorHuespedes().cargar(archivo)
        );
    }

    private Path escribir(String datos) throws IOException {
        Path archivo = carpeta.resolve("huespedes.csv");

        Files.writeString(
                archivo,
                ENCABEZADO + datos,
                StandardCharsets.UTF_8
        );

        return archivo;
    }
}