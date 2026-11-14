package co.edu.uq.sga.estructuras.lineales;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.uq.sga.estructuras.excepciones.EstructuraVaciaException;
import java.util.Comparator;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class ListaEnlazadaTest {

    @Test
    void agregaEnAmbosExtremosYConservaElOrden() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        lista.agregarAlFinal("b");
        lista.agregarAlInicio("a");
        lista.agregarAlFinal("c");

        assertEquals(3, lista.tamanio());
        assertEquals("a", lista.primero());
        assertEquals("c", lista.ultimo());
        assertEquals("[a, b, c]", lista.toString());
    }

    @Test
    void insertaYObtienePorIndice() {
        ListaEnlazada<Integer> lista = ListaEnlazada.de(1, 2, 4, 5);
        lista.insertar(2, 3);
        lista.insertar(0, 0);
        lista.insertar(6, 6);

        for (int i = 0; i <= 6; i++) {
            assertEquals(i, lista.obtener(i));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> lista.obtener(7));
        assertThrows(IndexOutOfBoundsException.class, () -> lista.insertar(-1, 9));
    }

    @Test
    void eliminaPorExtremosIndiceYValorManteniendoEnlaces() {
        ListaEnlazada<Integer> lista = ListaEnlazada.de(1, 2, 3, 4, 5);

        assertEquals(1, lista.eliminarPrimero());
        assertEquals(5, lista.eliminarUltimo());
        assertEquals(3, lista.eliminarEn(1));
        assertTrue(lista.eliminar(4));
        assertFalse(lista.eliminar(99));

        assertEquals("[2]", lista.toString());
        assertEquals(2, lista.primero());
        assertEquals(2, lista.ultimo());
        assertEquals(2, lista.eliminarPrimero());
        assertTrue(lista.estaVacia());
    }

    @Test
    void eliminarSiQuitaTodosLosQueCumplen() {
        ListaEnlazada<Integer> lista = ListaEnlazada.de(1, 2, 3, 4, 5, 6);
        assertEquals(3, lista.eliminarSi(n -> n % 2 == 0));
        assertEquals("[1, 3, 5]", lista.toString());
        assertEquals(5, lista.ultimo());
    }

    @Test
    void buscaFiltraYContiene() {
        ListaEnlazada<String> lista = ListaEnlazada.de("ana", "beto", "carla");

        assertTrue(lista.contiene("beto"));
        assertEquals(2, lista.indiceDe("carla"));
        assertEquals(-1, lista.indiceDe("zoe"));
        assertEquals("carla", lista.buscar(s -> s.startsWith("c")).orElseThrow());
        assertTrue(lista.buscar(s -> s.startsWith("z")).isEmpty());
        assertEquals("[beto, carla]", lista.filtrar(s -> s.length() > 3).toString());
    }

    @Test
    void recorreHaciaAdelanteYHaciaAtras() {
        ListaEnlazada<Integer> lista = ListaEnlazada.de(1, 2, 3);
        StringBuilder adelante = new StringBuilder();
        for (int n : lista) {
            adelante.append(n);
        }
        StringBuilder atras = new StringBuilder();
        for (int n : lista.enOrdenInverso()) {
            atras.append(n);
        }
        assertEquals("123", adelante.toString());
        assertEquals("321", atras.toString());
    }

    @Test
    void iteradorAgotadoLanzaExcepcionPropia() {
        Iterator<String> it = new ListaEnlazada<String>().iterator();
        assertFalse(it.hasNext());
        assertThrows(EstructuraVaciaException.class, it::next);
    }

    @Test
    void operacionesSobreListaVaciaLanzanExcepcionPropia() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        assertThrows(EstructuraVaciaException.class, lista::primero);
        assertThrows(EstructuraVaciaException.class, lista::ultimo);
        assertThrows(EstructuraVaciaException.class, lista::eliminarPrimero);
        assertThrows(EstructuraVaciaException.class, lista::eliminarUltimo);
    }

    @Test
    void rechazaElementosNulos() {
        ListaEnlazada<String> lista = new ListaEnlazada<>();
        assertThrows(IllegalArgumentException.class, () -> lista.agregar(null));
    }

    @Test
    void ordenaConMergeSortDeFormaEstable() {
        ListaEnlazada<String> lista = ListaEnlazada.de("pera", "kiwi", "uva", "mango", "higo", "fresa");
        lista.ordenar(Comparator.comparingInt(String::length));

        assertEquals("[uva, pera, kiwi, higo, mango, fresa]", lista.toString());
        assertEquals("fresa", lista.ultimo());
        StringBuilder atras = new StringBuilder();
        for (String s : lista.enOrdenInverso()) {
            atras.append(s.charAt(0));
        }
        assertEquals("fmhkpu", atras.toString());
    }

    @Test
    void ordenaConComparadoresCombinados() {
        ListaEnlazada<String> lista = ListaEnlazada.de("bb", "a", "ab", "c", "aa");
        lista.ordenar(Comparator.comparingInt(String::length).thenComparing(Comparator.reverseOrder()));
        assertEquals("[c, a, bb, ab, aa]", lista.toString());
    }

    @Test
    void ordenadaNoModificaLaOriginal() {
        ListaEnlazada<Integer> lista = ListaEnlazada.de(3, 1, 2);
        ListaEnlazada<Integer> copia = lista.ordenada(Comparator.naturalOrder());
        assertEquals("[3, 1, 2]", lista.toString());
        assertEquals("[1, 2, 3]", copia.toString());
    }

    @Test
    void convierteAArreglo() {
        ListaEnlazada<String> lista = ListaEnlazada.de("x", "y");
        assertArrayEquals(new String[] {"x", "y"}, lista.aArreglo(String[]::new));
    }
}
