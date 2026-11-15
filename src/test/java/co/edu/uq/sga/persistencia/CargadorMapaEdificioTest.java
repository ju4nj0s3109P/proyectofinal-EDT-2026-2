package co.edu.uq.sga.persistencia;

import co.edu.uq.sga.adaptadores.CalculadorRutasGrafo;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CargadorMapaEdificioTest {

    @Test
    void encuentraLaRutaMasCortaEnElMapaCargado() throws IOException {
        var mapa = new CargadorMapaEdificio().cargar(Path.of("data", "mapa-edificio.txt"));
        var rutas = new CalculadorRutasGrafo(mapa.grafo());

        assertEquals(5, mapa.ubicaciones().tamanio());
        assertArrayEquals(new String[]{"REC", "ESC", "A101"},
                rutas.calcularRuta("REC", "A101"));
        assertEquals(15.0, rutas.costoRuta("REC", "A101"), 0.001);
    }
}