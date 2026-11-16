package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.adaptadores.CalculadorRutasGrafo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CargadorMapaCSVTest {

    @TempDir
    Path carpeta;

    private static final String UBICACIONES = """
            id;tipo;nombre
            REC;RECEPCION;Recepción
            ESC;ESCALERA;Escalera principal
            A101;APARTAMENTO;Apartamento 101
            """;

    @Test
    void cargaElGrafoYCalculaLaRutaMasCorta() throws IOException {
        var mapa = cargar(UBICACIONES, """
                origen;destino;metros
                REC;A101;30
                REC;ESC;8
                ESC;A101;7
                """);

        assertEquals(3, mapa.grafo().cantidadVertices());
        assertEquals(3, mapa.grafo().cantidadAristas());

        var rutas = new CalculadorRutasGrafo(mapa.grafo());

        assertArrayEquals(
                new String[]{"REC", "ESC", "A101"},
                rutas.calcularRuta("REC", "A101")
        );

        assertEquals(15.0, rutas.costoRuta("REC", "A101"), 0.001);
    }

    @Test
    void cambiarElPesoEnCsvCambiaLaRuta() throws IOException {
        var mapa = cargar(UBICACIONES, """
                origen;destino;metros
                REC;A101;10
                REC;ESC;8
                ESC;A101;7
                """);

        var rutas = new CalculadorRutasGrafo(mapa.grafo());

        assertArrayEquals(
                new String[]{"REC", "A101"},
                rutas.calcularRuta("REC", "A101")
        );

        assertEquals(10.0, rutas.costoRuta("REC", "A101"), 0.001);
    }

    @Test
    void rechazaPasillosConUbicacionesInexistentes() {
        assertThrows(IOException.class, () -> cargar(UBICACIONES, """
                origen;destino;metros
                REC;A999;10
                """));
    }

    @Test
    void rechazaPasilloDuplicadoEnSentidoContrario() {
        assertThrows(IOException.class, () -> cargar(UBICACIONES, """
                origen;destino;metros
                REC;A101;10
                A101;REC;20
                """));
    }

    @Test
    void rechazaDistanciasNoValidas() {
        for (String peso : new String[]{"0", "-5", "NaN", "Infinity"}) {
            assertThrows(
                    IOException.class,
                    () -> cargar(
                            UBICACIONES,
                            "origen;destino;metros\nREC;A101;" + peso
                    ),
                    "Debe rechazar el peso: " + peso
            );
        }
    }

    @Test
    void rechazaMapaSinRecepcion() {
        assertThrows(IOException.class, () -> cargar(
                """
                id;tipo;nombre
                A101;APARTAMENTO;Apartamento 101
                """,
                "origen;destino;metros\n"
        ));
    }

    private CargadorMapaEdificio.MapaCargado cargar(
            String ubicaciones,
            String pasillos
    ) throws IOException {

        Path archivoUbicaciones = carpeta.resolve("ubicaciones.csv");
        Path archivoPasillos = carpeta.resolve("pasillos.csv");

        Files.writeString(
                archivoUbicaciones, ubicaciones, StandardCharsets.UTF_8
        );

        Files.writeString(
                archivoPasillos, pasillos, StandardCharsets.UTF_8
        );

        return new CargadorMapaEdificio().cargar(
                archivoUbicaciones, archivoPasillos
        );
    }
}