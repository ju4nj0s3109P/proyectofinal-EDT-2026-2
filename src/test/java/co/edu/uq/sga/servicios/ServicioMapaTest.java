package co.edu.uq.sga.servicios;

import co.edu.uq.sga.adaptadores.CalculadorRutasGrafo;
import co.edu.uq.sga.estructuras.grafos.Grafo;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ServicioMapaTest {
    private ServicioMapa crearServicio() {
        Grafo<String> grafo = new Grafo<>();
        for (String codigo : new String[]{"REC", "ESC", "A101", "AISLADO"}) {
            grafo.agregarVertice(codigo);
        }
        grafo.agregarArista("REC", "A101", 30);
        grafo.agregarArista("REC", "ESC", 8);
        grafo.agregarArista("ESC", "A101", 7);
        return new ServicioMapa(new ServicioMapa.Ubicacion[]{
                new ServicioMapa.Ubicacion("REC", "RECEPCION", "Recepción"),
                new ServicioMapa.Ubicacion("ESC", "ESCALERA", "Escalera"),
                new ServicioMapa.Ubicacion("A101", "APARTAMENTO", "Apartamento 101"),
                new ServicioMapa.Ubicacion("AISLADO", "APARTAMENTO", "Aislado")
        }, new ServicioMapa.Pasillo[]{
                new ServicioMapa.Pasillo("REC", "A101", 30),
                new ServicioMapa.Pasillo("REC", "ESC", 8),
                new ServicioMapa.Pasillo("ESC", "A101", 7)
        }, new CalculadorRutasGrafo(grafo));
    }

    @Test void usaDijkstraParaElegirElMenorCosto() {
        var ruta = crearServicio().calcularDesdeRecepcion("A101");
        assertArrayEquals(new String[]{"REC", "ESC", "A101"}, ruta.ubicaciones());
        assertEquals(15, ruta.metros(), 0.001);
    }

    @Test void informaCuandoElDestinoEstaAislado() {
        var ruta = crearServicio().calcularDesdeRecepcion("AISLADO");
        assertFalse(ruta.existe());
        assertEquals(Double.POSITIVE_INFINITY, ruta.metros());
    }

    @Test void laRecepcionTieneDistanciaCero() {
        var ruta = crearServicio().calcularDesdeRecepcion("REC");
        assertArrayEquals(new String[]{"REC"}, ruta.ubicaciones());
        assertEquals(0, ruta.metros(), 0.001);
    }

    @Test void lasConsultasProtegenLosArreglosInternos() {
        var servicio = crearServicio();
        servicio.consultarUbicaciones()[0] = null;
        servicio.consultarPasillos()[0] = null;
        assertEquals("REC", servicio.consultarRecepcion());
        assertNotNull(servicio.consultarPasillos()[0]);
        var ruta = servicio.calcularDesdeRecepcion("A101");
        ruta.ubicaciones()[0] = "OTRO";
        assertEquals("REC", ruta.ubicaciones()[0]);
    }
}
