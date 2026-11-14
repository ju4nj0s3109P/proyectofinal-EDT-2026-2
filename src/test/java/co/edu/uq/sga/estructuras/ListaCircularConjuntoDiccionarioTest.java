package co.edu.uq.sga.estructuras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.estructuras.arboles.DiccionarioArbol;
import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import co.edu.uq.sga.estructuras.lineales.Conjunto;
import co.edu.uq.sga.estructuras.lineales.ListaCircular;
import co.edu.uq.sga.estructuras.lineales.ListaEnlazada;
import org.junit.jupiter.api.Test;

class ListaCircularConjuntoDiccionarioTest {

    // ---------------------------------------------------------- lista circular

    @Test
    void rotaTurnosVolviendoAlPrimero() {
        ListaCircular<String> turnos = new ListaCircular<>();
        turnos.agregar("Ana");
        turnos.agregar("Beto");
        turnos.agregar("Carla");

        assertEquals("Ana", turnos.actual());
        assertEquals("Beto", turnos.verSiguiente());
        assertEquals("Beto", turnos.avanzar());
        assertEquals("Carla", turnos.avanzar());
        assertEquals("Ana", turnos.avanzar(), "después del último vuelve al primero");
        assertEquals("Carla", turnos.retroceder());
    }

    @Test
    void agregarRespetaElOrdenDeRegistroAunqueElTurnoHayaAvanzado() {
        ListaCircular<String> turnos = new ListaCircular<>();
        turnos.agregar("Ana");
        turnos.agregar("Beto");
        turnos.avanzar(); // turno de Beto
        turnos.agregar("Carla");

        StringBuilder vuelta = new StringBuilder();
        for (String r : turnos) {
            vuelta.append(r).append(' ');
        }
        assertEquals("Beto Carla Ana ", vuelta.toString(), "una sola vuelta desde el actual");
    }

    @Test
    void eliminarElActualPasaElTurnoAlSiguiente() {
        ListaCircular<String> turnos = new ListaCircular<>();
        turnos.agregar("Ana");
        turnos.agregar("Beto");
        assertTrue(turnos.eliminar("Ana"));
        assertEquals("Beto", turnos.actual());
        assertEquals("Beto", turnos.avanzar());
        assertTrue(turnos.eliminar("Beto"));
        assertTrue(turnos.estaVacia());
        assertThrows(EstructuraVaciaException.class, turnos::actual);
        assertThrows(EstructuraVaciaException.class, turnos::avanzar);
    }

    // ---------------------------------------------------------------- conjunto

    @Test
    void conjuntoNoGuardaRepetidos() {
        Conjunto<String> favoritos = new Conjunto<>();
        assertTrue(favoritos.agregar("A-101"));
        assertTrue(favoritos.agregar("B-202"));
        assertFalse(favoritos.agregar("A-101"));
        assertEquals(2, favoritos.tamanio());
        assertEquals("{A-101, B-202}", favoritos.toString());
        assertTrue(favoritos.eliminar("A-101"));
        assertFalse(favoritos.contiene("A-101"));
    }

    @Test
    void unionEInterseccion() {
        Conjunto<Integer> a = new Conjunto<>();
        Conjunto<Integer> b = new Conjunto<>();
        for (int n : new int[] {1, 2, 3}) {
            a.agregar(n);
        }
        for (int n : new int[] {2, 3, 4}) {
            b.agregar(n);
        }
        assertEquals("{1, 2, 3, 4}", a.union(b).toString());
        assertEquals("{2, 3}", a.interseccion(b).toString());
    }

    // --------------------------------------------------------------- diccionario

    @Test
    void diccionarioPoneObtieneReemplazaYQuita() {
        DiccionarioArbol<String, Integer> capacidad = new DiccionarioArbol<>();
        assertTrue(capacidad.poner("B-202", 4).isEmpty());
        capacidad.poner("A-101", 2);
        assertEquals(4, capacidad.poner("B-202", 6).orElseThrow(), "devuelve el valor anterior");

        assertEquals(6, capacidad.obtener("B-202").orElseThrow());
        assertTrue(capacidad.obtener("Z-999").isEmpty());
        assertEquals("[A-101, B-202]", capacidad.claves().toString());
        assertEquals("[2, 6]", capacidad.valores().toString());

        assertEquals(2, capacidad.quitar("A-101").orElseThrow());
        assertFalse(capacidad.contieneClave("A-101"));
        assertEquals(1, capacidad.tamanio());
    }

    @Test
    void obtenerOCrearReutilizaElValorExistente() {
        DiccionarioArbol<String, ListaEnlazada<String>> reservasPorApto = new DiccionarioArbol<>();
        reservasPorApto.obtenerOCrear("A-101", ListaEnlazada::new).agregar("F-1");
        reservasPorApto.obtenerOCrear("A-101", ListaEnlazada::new).agregar("F-2");
        assertEquals("[F-1, F-2]", reservasPorApto.obtener("A-101").orElseThrow().toString());
        assertEquals(1, reservasPorApto.tamanio());
    }

    @Test
    void diccionarioSeRecorreOrdenadoPorClave() {
        DiccionarioArbol<Integer, String> d = new DiccionarioArbol<>();
        d.poner(3, "c");
        d.poner(1, "a");
        d.poner(2, "b");
        StringBuilder sb = new StringBuilder();
        for (DiccionarioArbol.Entrada<Integer, String> e : d) {
            sb.append(e.getClave()).append(e.getValor());
        }
        assertEquals("1a2b3c", sb.toString());
    }
}
