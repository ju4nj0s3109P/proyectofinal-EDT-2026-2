package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.*;
import co.edu.uq.sga.adaptadores.CalculadorRutasGrafo;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.estructuras.grafos.Grafo;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ServicioMapaAgrupacionTest {
    @Test
    void agrupaApartamentosPorBloqueUsandoRecorridoDelGrafo() {
        Grafo<String> grafo = new Grafo<>();
        for (String nodo : new String[]{"R", "A1", "A2", "B1"}) grafo.agregarVertice(nodo);
        grafo.agregarArista("R", "A2", 1);
        grafo.agregarArista("A2", "A1", 1);
        grafo.agregarArista("R", "B1", 1);

        ServicioMapa mapa = new ServicioMapa(
                new ServicioMapa.Ubicacion[]{
                        new ServicioMapa.Ubicacion("R", "RECEPCION", "Recepción"),
                        new ServicioMapa.Ubicacion("A1", "APARTAMENTO", "A1"),
                        new ServicioMapa.Ubicacion("A2", "APARTAMENTO", "A2"),
                        new ServicioMapa.Ubicacion("B1", "APARTAMENTO", "B1")},
                new ServicioMapa.Pasillo[0], new CalculadorRutasGrafo(grafo));

        Apartamento[] apartamentos = {
                apartamento("A1", "A"), apartamento("A2", "A"), apartamento("B1", "B")};
        var grupos = mapa.agruparApartamentosPorBloque(apartamentos, false);
        assertEquals(2, grupos.length);
        assertEquals("A", grupos[0].bloque());
        assertArrayEquals(new String[]{"A2", "A1"}, grupos[0].codigosApartamentos());
        assertEquals("B", grupos[1].bloque());
        assertArrayEquals(new String[]{"B1"}, grupos[1].codigosApartamentos());
    }

    private static Apartamento apartamento(String codigo, String bloque) {
        return new Apartamento(codigo, codigo, bloque, 1, 1, 2, new String[0],
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
    }
}
