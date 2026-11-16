package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.dominio.enums.TipoTemporada;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CargadorTemporadasTest {

    @TempDir
    Path carpeta;

    @Test
    void cargaTemporadasYRespetaElCambioDeFecha() throws IOException {
        Path archivo = escribir("""
                tipo;inicio;fin
                BAJA;2026-11-01;2026-11-20
                MEDIA;2026-11-20;2026-12-01
                """);

        var servicio = new CargadorTemporadas().cargar(archivo);

        assertEquals(
                TipoTemporada.BAJA,
                servicio.temporadaDe(
                        LocalDate.of(2026, 11, 19)
                ).getTipo()
        );

        assertEquals(
                TipoTemporada.MEDIA,
                servicio.temporadaDe(
                        LocalDate.of(2026, 11, 20)
                ).getTipo()
        );
    }

    @Test
    void rechazaTemporadasSolapadas() throws IOException {
        Path archivo = escribir("""
                tipo;inicio;fin
                BAJA;2026-11-01;2026-12-01
                ALTA;2026-11-20;2026-12-15
                """);

        assertThrows(
                IOException.class,
                () -> new CargadorTemporadas().cargar(archivo)
        );
    }

    @Test
    void rechazaFechaInvalida() throws IOException {
        Path archivo = escribir("""
                tipo;inicio;fin
                BAJA;2026-02-30;2026-03-10
                """);

        assertThrows(
                IOException.class,
                () -> new CargadorTemporadas().cargar(archivo)
        );
    }

    @Test
    void rechazaArchivoSinTemporadas() throws IOException {
        Path archivo = escribir("tipo;inicio;fin\n");

        assertThrows(
                IOException.class,
                () -> new CargadorTemporadas().cargar(archivo)
        );
    }

    private Path escribir(String contenido) throws IOException {
        Path archivo = carpeta.resolve("temporadas.csv");
        Files.writeString(archivo, contenido);
        return archivo;
    }
}