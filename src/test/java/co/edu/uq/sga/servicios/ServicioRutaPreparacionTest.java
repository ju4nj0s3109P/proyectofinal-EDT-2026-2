package co.edu.uq.sga.servicios;

import static org.junit.jupiter.api.Assertions.*;
import co.edu.uq.sga.adaptadores.CatalogoApartamentosArbol;
import co.edu.uq.sga.adaptadores.CalculadorRutasGrafo;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.estructuras.grafos.Grafo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ServicioRutaPreparacionTest {
    @Test
    void calculaRecorridoDePendientesRespetandoPrioridadDeLlegada() {
        LocalDate hoy = LocalDate.now();
        CatalogoApartamentosArbol catalogo = new CatalogoApartamentosArbol();
        Apartamento primero = apartamento("A1");
        Apartamento segundo = apartamento("A2");
        catalogo.agregar(primero);
        catalogo.agregar(segundo);
        Grafo<String> grafo = new Grafo<>();
        for (String nodo : new String[]{"REC", "A1", "A2"}) grafo.agregarVertice(nodo);
        grafo.agregarArista("REC", "A1", 2);
        grafo.agregarArista("REC", "A2", 10);
        grafo.agregarArista("A2", "A1", 3);

        ServicioPanelPreparacion panel = new ServicioPanelPreparacion(
                catalogo, new CalculadorRutasGrafo(grafo), hoy);
        primero.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);
        segundo.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);
        panel.encolarPendiente("A1", hoy.atTime(11, 0));
        panel.encolarPendiente("A2", hoy.atTime(10, 0));

        // La ruta mínima desde REC hasta A2 atraviesa A1. Ese paso intermedio
        // no significa que su tarea se atienda antes: el recorrido vuelve a A1
        // después de llegar a A2, respetando el orden de prioridad de la cola.
        assertArrayEquals(new String[]{"REC", "A1", "A2", "A1"},
                panel.calcularRutaPendientes("REC"));
    }

    @Test
    void eligeElMenorCostoTotalRespetandoElOrdenDePrioridad() {
        LocalDate hoy = LocalDate.now();
        CatalogoApartamentosArbol catalogo = new CatalogoApartamentosArbol();
        Apartamento primero = apartamento("A1");
        Apartamento segundo = apartamento("A2");
        catalogo.agregar(primero);
        catalogo.agregar(segundo);

        Grafo<String> grafo = new Grafo<>();
        for (String nodo : new String[]{"REC", "X", "A1", "A2"}) grafo.agregarVertice(nodo);
        grafo.agregarArista("REC", "A1", 5);
        grafo.agregarArista("REC", "X", 1);
        grafo.agregarArista("X", "A1", 1);
        grafo.agregarArista("A1", "A2", 2);
        grafo.agregarArista("X", "A2", 20);

        ServicioPanelPreparacion panel = new ServicioPanelPreparacion(
                catalogo, new CalculadorRutasGrafo(grafo), hoy);
        primero.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);
        segundo.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);
        panel.encolarPendiente("A1", hoy.atTime(10, 0));
        panel.encolarPendiente("A2", hoy.atTime(11, 0));

        assertArrayEquals(new String[]{"REC", "X", "A1", "A2"},
                panel.calcularRutaPendientes("REC"));
    }

    @Test
    void recalculaElRecorridoCuandoSeCierraElPasilloMasCorto() {
        LocalDate hoy = LocalDate.now();
        CatalogoApartamentosArbol catalogo = new CatalogoApartamentosArbol();
        Apartamento primero = apartamento("A1");
        Apartamento segundo = apartamento("A2");
        catalogo.agregar(primero);
        catalogo.agregar(segundo);

        Grafo<String> grafo = new Grafo<>();
        for (String nodo : new String[]{"REC", "X", "A1", "A2"}) grafo.agregarVertice(nodo);
        grafo.agregarArista("REC", "A1", 5);
        grafo.agregarArista("REC", "X", 1);
        grafo.agregarArista("X", "A1", 1);
        grafo.agregarArista("A1", "A2", 2);
        grafo.agregarArista("X", "A2", 20);
        grafo.cerrarArista("X", "A1");

        ServicioPanelPreparacion panel = new ServicioPanelPreparacion(
                catalogo, new CalculadorRutasGrafo(grafo), hoy);
        primero.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);
        segundo.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);
        panel.encolarPendiente("A1", hoy.atTime(10, 0));
        panel.encolarPendiente("A2", hoy.atTime(11, 0));

        assertArrayEquals(new String[]{"REC", "A1", "A2"},
                panel.calcularRutaPendientes("REC"));
    }

    @Test
    void informaCuandoNoExisteUnRecorridoParaTodasLasPendientes() {
        LocalDate hoy = LocalDate.now();
        CatalogoApartamentosArbol catalogo = new CatalogoApartamentosArbol();
        Apartamento primero = apartamento("A1");
        Apartamento segundo = apartamento("A2");
        catalogo.agregar(primero);
        catalogo.agregar(segundo);

        Grafo<String> grafo = new Grafo<>();
        for (String nodo : new String[]{"REC", "A1", "A2"}) grafo.agregarVertice(nodo);
        grafo.agregarArista("REC", "A1", 2);

        ServicioPanelPreparacion panel = new ServicioPanelPreparacion(
                catalogo, new CalculadorRutasGrafo(grafo), hoy);
        primero.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);
        segundo.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);
        panel.encolarPendiente("A1", hoy.atTime(10, 0));
        panel.encolarPendiente("A2", hoy.atTime(11, 0));

        assertThrows(co.edu.uq.sga.dominio.excepciones.ExcepcionDominio.class,
                () -> panel.calcularRutaPendientes("REC"));
    }

    @Test
    void devuelveSoloElOrigenCuandoNoHayTareasPendientes() {
        CatalogoApartamentosArbol catalogo = new CatalogoApartamentosArbol();
        ServicioPanelPreparacion panel = new ServicioPanelPreparacion(
                catalogo, new CalculadorRutasGrafo(grafoSimple()), LocalDate.now());

        assertArrayEquals(new String[]{"REC"}, panel.calcularRutaPendientes("REC"));
    }

    private static Grafo<String> grafoSimple() {
        Grafo<String> grafo = new Grafo<>();
        grafo.agregarVertice("REC");
        return grafo;
    }
    private static Apartamento apartamento(String codigo) {
        return new Apartamento(codigo, codigo, "A", 1, 1, 2, new String[0],
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
    }
}
