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

        assertArrayEquals(new String[]{"REC", "A2", "A1"},
                panel.calcularRutaPendientes("REC"));
    }

    private static Apartamento apartamento(String codigo) {
        return new Apartamento(codigo, codigo, "A", 1, 1, 2, new String[0],
                BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
    }
}
