package co.edu.uq.sga.adaptadores;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.dominio.enums.CanalReserva;
import co.edu.uq.sga.dominio.enums.EstadoApartamento;
import co.edu.uq.sga.dominio.excepciones.ConflictoDisponibilidadException;
import co.edu.uq.sga.dominio.modelo.Apartamento;
import co.edu.uq.sga.dominio.modelo.AsignacionApartamento;
import co.edu.uq.sga.dominio.modelo.Estancia;
import co.edu.uq.sga.dominio.modelo.Huesped;
import co.edu.uq.sga.dominio.modelo.Ocupante;
import co.edu.uq.sga.dominio.modelo.Reserva;
import co.edu.uq.sga.dominio.modelo.TareaPreparacion;
import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import co.edu.uq.sga.estructuras.grafos.Grafo;
import co.edu.uq.sga.servicios.ServicioPreparacion;
import co.edu.uq.sga.servicios.ServicioReservas;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import org.junit.jupiter.api.Test;

class AdaptadoresTest {

    private final Apartamento a101 = apartamento("A-101");
    private final Apartamento a102 = apartamento("A-102");

    @Test
    void indiceRechazaSolapamientosYAceptaNochesContiguas() {
        IndiceReservasArbol indice = new IndiceReservasArbol();
        assertTrue(indice.agregarSiDisponible(reserva("F-1", 10, 12, a101)));

        assertFalse(indice.agregarSiDisponible(reserva("F-2", 11, 13, a101)), "comparte la noche 11");
        assertFalse(indice.agregarSiDisponible(reserva("F-3", 8, 11, a101)), "comparte la noche 10");
        assertFalse(indice.agregarSiDisponible(reserva("F-4", 9, 14, a101)), "la contiene");
        assertTrue(indice.agregarSiDisponible(reserva("F-5", 12, 14, a101)), "la salida es exclusiva");
        assertTrue(indice.agregarSiDisponible(reserva("F-6", 8, 10, a101)));
        assertTrue(indice.agregarSiDisponible(reserva("F-7", 11, 13, a102)), "otro apartamento");

        assertEquals(3, indice.reservasDe("A-101").tamanio());
        assertEquals("F-6", indice.reservasDe("A-101").primero().getFolio(), "ordenadas por entrada");
        assertEquals("[A-101, A-102]", indice.apartamentosConReservas().toString());
    }

    @Test
    void indiceDetectaConflictoCuandoDosReservasEmpiezanElMismoDia() {
        IndiceReservasArbol indice = new IndiceReservasArbol();
        assertTrue(indice.agregarSiDisponible(reserva("F-1", 10, 15, a101)));

        assertFalse(indice.agregarSiDisponible(reserva("F-2", 10, 12, a101)));
        assertEquals(1, indice.reservasDe("A-101").tamanio());
    }

    @Test
    void reservaConVariosApartamentosSeAgregaDeFormaAtomica() {
        IndiceReservasArbol indice = new IndiceReservasArbol();
        indice.agregarSiDisponible(reserva("F-1", 10, 12, a102));

        Reserva grupo = reserva("F-G", 11, 13, a101, a102);
        assertFalse(indice.agregarSiDisponible(grupo));
        assertFalse(indice.contiene(grupo));
        assertTrue(indice.reservasDe("A-101").estaVacia(), "no quedó indexada a medias");

        Reserva grupoLibre = reserva("F-H", 12, 14, a101, a102);
        assertTrue(indice.agregarSiDisponible(grupoLibre));
        assertTrue(indice.contiene(grupoLibre));
    }

    @Test
    void estaDisponibleIgnoraLaMismaReservaYNoModificaElIndice() {
        IndiceReservasArbol indice = new IndiceReservasArbol();
        Reserva propia = reserva("F-1", 10, 12, a101);
        indice.agregarSiDisponible(propia);

        assertTrue(indice.estaDisponible(propia), "no choca consigo misma");
        assertFalse(indice.estaDisponible(reserva("F-2", 10, 11, a101)));
        assertFalse(indice.agregarSiDisponible(propia), "no se indexa dos veces");
        assertEquals(1, indice.reservasDe("A-101").tamanio());
    }

    @Test
    void retirarLiberaLasNochesDeTodosLosApartamentos() {
        IndiceReservasArbol indice = new IndiceReservasArbol();
        Reserva grupo = reserva("F-G", 10, 12, a101, a102);
        indice.agregarSiDisponible(grupo);

        assertTrue(indice.retirarSiExiste(grupo));
        assertFalse(indice.retirarSiExiste(grupo));
        assertFalse(indice.contiene(grupo));
        assertTrue(indice.apartamentosConReservas().estaVacia());
        assertTrue(indice.agregarSiDisponible(reserva("F-2", 10, 12, a101)));
    }

    @Test
    void servicioDeReservasFuncionaConElIndiceReal() {
        ServicioReservas servicio = new ServicioReservas(new IndiceReservasArbol());
        Reserva primera = reserva("F-1", 10, 12, a101);
        servicio.registrarDirecta(primera);

        Reserva chocante = reserva("F-2", 11, 12, a101);
        assertFalse(servicio.consultarDisponibilidad(chocante));
        assertThrows(ConflictoDisponibilidadException.class, () -> servicio.registrarDirecta(chocante));
    }

    @Test
    void colaDeSolicitudesExternasEsFifoYBuscaPorFolio() {
        ColaSolicitudesExternasEnCola cola = new ColaSolicitudesExternasEnCola();
        cola.encolar(reserva("EXT-1", 10, 12, a101));
        cola.encolar(reserva("EXT-2", 10, 12, a102));

        assertTrue(cola.contieneFolio("EXT-2"));
        assertTrue(cola.contieneFolio(" EXT-1 "));
        assertFalse(cola.contieneFolio("EXT-9"));
        assertEquals("EXT-1", cola.desencolar().getFolio());
        assertEquals("EXT-2", cola.desencolar().getFolio());
        assertTrue(cola.estaVacia());
        assertThrows(EstructuraVaciaException.class, cola::desencolar);
    }

    @Test
    void colaDePreparacionPorPrioridadAtiendeSegunElComparador() {
        ColaPreparacionesPorPrioridad cola = new ColaPreparacionesPorPrioridad(
                Comparator.comparing((TareaPreparacion t) -> t.getApartamento().getCodigo()).reversed());
        cola.encolar(tarea("T-1", a101));
        cola.encolar(tarea("T-2", a102));

        assertEquals("T-2", cola.desencolar().getCodigo());
        assertEquals("T-1", cola.desencolar().getCodigo());
        assertThrows(EstructuraVaciaException.class, cola::desencolar);
    }

    @Test
    void calculadorDeRutasDevuelveElCaminoMasCortoComoArreglo() {
        CalculadorRutasGrafo rutas = new CalculadorRutasGrafo(mapa());
        assertArrayEquals(new String[] {"REC", "ESC", "A-101"}, rutas.calcularRuta("REC", "A-101"));
        assertEquals(15.0, rutas.costoRuta("REC", "A-101"));
        assertArrayEquals(new String[] {"REC", "ESC", "A-101", "A-102"},
                rutas.calcularRecorrido("REC", new String[] {"A-101", "A-102"}));
        assertEquals(0, rutas.calcularRuta("REC", "NO-EXISTE").length);

        rutas.getMapa().cerrarArista("REC", "ESC");
        assertArrayEquals(new String[] {"REC", "A-102", "A-101"}, rutas.calcularRuta("REC", "A-101"),
                "con el pasillo cerrado toma el desvío");
        assertEquals(32.0, rutas.costoRuta("REC", "A-101"));
        rutas.getMapa().cerrarArista("REC", "A-102");
        assertEquals(0, rutas.calcularRuta("REC", "A-101").length, "recepción quedó aislada");
    }

    @Test
    void servicioDePreparacionUsaColaYRutasReales() {
        CalculadorRutasGrafo rutas = new CalculadorRutasGrafo(mapa());
        ServicioPreparacion servicio = new ServicioPreparacion(new ColaPreparacionesEnCola(), rutas);
        a101.cambiarEstadoOperativo(EstadoApartamento.PENDIENTE_PREPARACION);
        servicio.encolar(tarea("T-1", a101));

        TareaPreparacion tarea = servicio.despacharSiguiente().orElseThrow();
        assertEquals("A-101", tarea.getApartamento().getCodigo());
        assertArrayEquals(new String[] {"REC", "ESC", "A-101"}, servicio.calcularRuta("REC", "A-101"));
        servicio.completar(tarea);
        assertEquals(EstadoApartamento.PREPARADO, a101.getEstadoOperativo());
        assertTrue(servicio.despacharSiguiente().isEmpty());
    }

    private static Grafo<String> mapa() {
        Grafo<String> g = new Grafo<>();
        for (String v : new String[] {"REC", "ESC", "A-101", "A-102"}) {
            g.agregarVertice(v);
        }
        g.agregarArista("REC", "ESC", 10);
        g.agregarArista("ESC", "A-101", 5);
        g.agregarArista("A-101", "A-102", 2);
        g.agregarArista("REC", "A-102", 30);
        return g;
    }

    private static Apartamento apartamento(String codigo) {
        return new Apartamento(codigo, "Suite " + codigo, "A", 1, 2, 4, new String[0],
                new BigDecimal("100000"), new BigDecimal("120000"), new BigDecimal("150000"));
    }

    private static Reserva reserva(String folio, int diaEntrada, int diaSalida, Apartamento... aptos) {
        Huesped titular = new Huesped("Titular", "1094000000", LocalDate.of(1990, 1, 1),
                "3001234567", "titular@example.com", null);
        Ocupante[] ocupantes = {new Ocupante("Titular", LocalDate.of(1990, 1, 1))};
        AsignacionApartamento[] asignaciones = new AsignacionApartamento[aptos.length];
        for (int i = 0; i < aptos.length; i++) {
            asignaciones[i] = new AsignacionApartamento(aptos[i]);
        }
        return new Reserva(folio, titular, ocupantes,
                new Estancia(LocalDate.of(2026, 12, diaEntrada), LocalDate.of(2026, 12, diaSalida)),
                CanalReserva.DIRECTA, asignaciones, LocalDateTime.of(2026, 11, 1, 9, 0));
    }

    private static TareaPreparacion tarea(String codigo, Apartamento apartamento) {
        return new TareaPreparacion(codigo, apartamento, LocalDateTime.of(2026, 12, 1, 8, 0));
    }
}
