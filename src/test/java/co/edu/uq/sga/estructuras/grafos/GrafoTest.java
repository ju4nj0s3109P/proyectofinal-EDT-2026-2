package co.edu.uq.sga.estructuras.grafos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import org.junit.jupiter.api.Test;

class GrafoTest {

    private static Grafo<String> edificio() {
        Grafo<String> g = new Grafo<>();
        for (String v : new String[] {"REC", "ESC1", "ESC2", "ASEO", "A101", "A102", "B201"}) {
            g.agregarVertice(v);
        }
        g.agregarArista("REC", "ESC1", 10);
        g.agregarArista("ESC1", "A101", 5);
        g.agregarArista("ESC1", "A102", 7);
        g.agregarArista("A101", "A102", 1);
        g.agregarArista("REC", "ASEO", 3);
        g.agregarArista("ASEO", "A102", 20);
        g.agregarArista("ESC1", "ESC2", 15);
        g.agregarArista("ESC2", "B201", 4);
        return g;
    }

    @Test
    void registraVerticesYAristasNoDirigidas() {
        Grafo<String> g = edificio();
        assertEquals(7, g.cantidadVertices());
        assertEquals(8, g.cantidadAristas());
        assertEquals(8, g.aristas().tamanio());
        assertFalse(g.agregarVertice("REC"), "no repite vértices");
        assertEquals("[REC, A101, A102, ESC2]", g.vecinos("ESC1").toString());
        assertEquals(1.0, g.pesoArista("A102", "A101").orElseThrow(), "se recorre en ambos sentidos");
    }

    @Test
    void agregarAristaExistenteActualizaElPeso() {
        Grafo<String> g = edificio();
        g.agregarArista("A102", "A101", 2);
        assertEquals(2.0, g.pesoArista("A101", "A102").orElseThrow());
        assertEquals(8, g.cantidadAristas());
    }

    @Test
    void recorridoEnAnchuraVaPorNiveles() {
        assertEquals("[REC, ESC1, ASEO, A101, A102, ESC2, B201]",
                edificio().recorridoAnchura("REC").toString());
    }

    @Test
    void recorridoEnProfundidadRecursivoBajaAntesDeRetroceder() {
        assertEquals("[REC, ESC1, A101, A102, ASEO, ESC2, B201]",
                edificio().recorridoProfundidad("REC").toString());
    }

    @Test
    void dijkstraEncuentraLaRutaMasCorta() {
        Ruta<String> ruta = edificio().dijkstra("REC", "A102");
        assertTrue(ruta.existe());
        assertEquals("[REC, ESC1, A101, A102]", ruta.getCamino().toString());
        assertEquals(16.0, ruta.getCosto());
    }

    @Test
    void dijkstraDeUnVerticeASiMismo() {
        Ruta<String> ruta = edificio().dijkstra("A101", "A101");
        assertEquals("[A101]", ruta.getCamino().toString());
        assertEquals(0.0, ruta.getCosto());
    }

    @Test
    void rutaConMenosTramosUsaBfs() {
        Ruta<String> ruta = edificio().rutaMenosTramos("REC", "A102");
        assertEquals("[REC, ESC1, A102]", ruta.getCamino().toString());
        assertEquals(17.0, ruta.getCosto());
    }

    @Test
    void cerrarPasilloCambiaLaRutaYReabrirLaRestaura() {
        Grafo<String> g = edificio();
        assertTrue(g.cerrarArista("A101", "A102"));
        assertEquals(17.0, g.dijkstra("REC", "A102").getCosto());
        assertFalse(g.vecinos("A101").contiene("A102"));
        assertTrue(g.abrirArista("A102", "A101"));
        assertEquals(16.0, g.dijkstra("REC", "A102").getCosto());
    }

    @Test
    void detectaApartamentosInalcanzablesAlCerrarUnPasillo() {
        Grafo<String> g = edificio();
        assertTrue(g.esConexo());
        g.cerrarArista("ESC1", "ESC2");

        assertEquals("[ESC2, B201]", g.inalcanzablesDesde("REC").toString());
        assertFalse(g.esConexo());
        assertFalse(g.dijkstra("REC", "B201").existe());
        assertTrue(Double.isInfinite(g.dijkstra("REC", "B201").getCosto()));

        ListaEnlazada<ListaEnlazada<String>> grupos = g.componentesConexas();
        assertEquals(2, grupos.tamanio());
        assertEquals("[ESC2, B201]", grupos.ultimo().toString());
    }

    @Test
    void rutaPorParadasEncadenaTramosEnElOrdenDado() {
        Ruta<String> ruta = edificio().rutaPorParadas("REC", ListaEnlazada.de("B201", "A101"));
        assertEquals("[REC, ESC1, ESC2, B201, ESC2, ESC1, A101]", ruta.getCamino().toString());
        assertEquals(53.0, ruta.getCosto());
    }

    @Test
    void rutaPorParadasQuedaVaciaSiUnaParadaEsInalcanzable() {
        Grafo<String> g = edificio();
        g.cerrarArista("ESC2", "B201");
        assertFalse(g.rutaPorParadas("REC", ListaEnlazada.de("A101", "B201")).existe());
    }

    @Test
    void eliminarVerticeQuitaSusPasillos() {
        Grafo<String> g = edificio();
        assertTrue(g.eliminarVertice("A102"));
        assertEquals(6, g.cantidadVertices());
        assertEquals(5, g.cantidadAristas());
        assertFalse(g.vecinos("A101").contiene("A102"));
        assertTrue(g.eliminarArista("REC", "ASEO"));
        assertEquals(4, g.cantidadAristas());
        assertEquals("[ASEO]", g.inalcanzablesDesde("REC").toString());
    }

    @Test
    void grafoDirigidoRespetaElSentido() {
        Grafo<String> g = new Grafo<>(true);
        g.agregarVertice("a");
        g.agregarVertice("b");
        g.agregarArista("a", "b", 1);
        assertEquals("[b]", g.vecinos("a").toString());
        assertTrue(g.vecinos("b").estaVacia());
        assertFalse(g.dijkstra("b", "a").existe());
        assertTrue(g.eliminarVertice("b"));
        assertEquals(0, g.cantidadAristas());
    }

    @Test
    void validaPesosYVertices() {
        Grafo<String> g = edificio();
        assertThrows(IllegalArgumentException.class, () -> g.agregarArista("REC", "A101", 0));
        assertThrows(IllegalArgumentException.class, () -> g.agregarArista("REC", "A101", -3));
        assertThrows(IllegalArgumentException.class, () -> g.agregarArista("REC", "REC", 1));
        assertThrows(IllegalArgumentException.class, () -> g.agregarArista("REC", "X", 1));
        assertThrows(IllegalArgumentException.class, () -> g.dijkstra("REC", "X"));
        assertThrows(IllegalArgumentException.class, () -> g.agregarVertice(null));
    }

    @Test
    void seRecorreConForEach() {
        StringBuilder sb = new StringBuilder();
        for (String v : edificio()) {
            sb.append(v).append(' ');
        }
        assertEquals("REC ESC1 ESC2 ASEO A101 A102 B201 ", sb.toString());
    }
}
